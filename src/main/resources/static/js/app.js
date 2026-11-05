/* Campus Loop - single page frontend for the Smart Campus Second-hand Trading Platform.
   Plain JavaScript, no build step: every page calls the REST API with the session cookie. */

const state = {
    user: null,
    meta: null,
    unread: 0,
    search: { keyword: '', category: '', min: '', max: '', conditions: [], sort: 'newest', page: 0 }
};

const CATEGORY_ICON = {
    BOOKS: '📚', ELECTRONICS: '💻', FURNITURE: '🪑', DAILY_SUPPLIES: '🧺',
    CLOTHING: '👕', SPORTS: '🏸', OTHER: '📦'
};

/* ------------------------------------------------------------------ helpers */

const esc = (value) => String(value === null || value === undefined ? '' : value)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');

const money = (value) => 'MOP ' + Number(value || 0).toFixed(2);

const statusLabel = (status) => ({ AVAILABLE: 'Available', RESERVED: 'Reserved', SOLD: 'Sold' }[status] || status);

const dateStr = (iso) => iso
    ? new Date(iso).toLocaleString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    : '';

const dayStr = (iso) => iso
    ? new Date(iso).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
    : '';

function toast(message, isError = false) {
    const stack = document.getElementById('toasts');
    const node = document.createElement('div');
    node.className = 'toast' + (isError ? ' error' : '');
    node.textContent = message;
    stack.appendChild(node);
    setTimeout(() => node.remove(), 3800);
}

async function api(path, options = {}) {
    const request = { method: options.method || 'GET', headers: {}, credentials: 'same-origin' };
    if (options.body !== undefined) {
        request.headers['Content-Type'] = 'application/json';
        request.body = JSON.stringify(options.body);
    }
    const response = await fetch('/api' + path, request);
    let payload = null;
    try {
        payload = await response.json();
    } catch (ignored) {
        payload = null;
    }
    if (!response.ok || !payload || payload.success === false) {
        throw new Error((payload && payload.message) || 'Request failed (' + response.status + ')');
    }
    return payload;
}

function go(hash) {
    window.location.hash = hash;
}

function requireLogin() {
    if (state.user) {
        return true;
    }
    document.getElementById('view').innerHTML = `
        <div class="empty">
            <span class="big">🔒</span>
            <h2>Please log in</h2>
            <p>This page belongs to your account.</p>
            <a class="btn btn-primary" href="#/login">Log in</a>
            <a class="btn" href="#/register">Create an account</a>
        </div>`;
    return false;
}

/* -------------------------------------------------------------- chrome/nav */

function renderChrome() {
    const account = document.getElementById('account');
    if (!state.user) {
        account.innerHTML = `<a class="btn btn-sm" href="#/login">Log in</a>`;
    } else {
        const initials = state.user.name.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase();
        account.innerHTML = `
            <button class="account-btn" id="account-toggle">
                <span class="avatar">${esc(initials)}</span>${esc(state.user.name.split(' ')[0])}
            </button>
            <div class="menu" id="account-menu">
                <a href="#/profile">Profile &amp; settings</a>
                <a href="#/my-listings">My listings</a>
                <a href="#/requests">My requests</a>
                <div class="divider"></div>
                <button type="button" id="logout-btn">Log out</button>
            </div>`;
        const toggle = document.getElementById('account-toggle');
        const menu = document.getElementById('account-menu');
        toggle.addEventListener('click', (event) => {
            event.stopPropagation();
            menu.classList.toggle('open');
        });
        document.getElementById('logout-btn').addEventListener('click', async () => {
            await api('/auth/logout', { method: 'POST' });
            state.user = null;
            state.unread = 0;
            renderChrome();
            toast('Logged out');
            go('#/home');
        });
    }

    const count = document.getElementById('bell-count');
    if (state.unread > 0) {
        count.hidden = false;
        count.textContent = state.unread > 99 ? '99+' : state.unread;
    } else {
        count.hidden = true;
    }
}

function markNav(page) {
    document.querySelectorAll('#nav a').forEach((link) => {
        link.classList.toggle('active', link.dataset.nav === page);
    });
}

async function refreshUnread() {
    if (!state.user) {
        state.unread = 0;
        renderChrome();
        return;
    }
    try {
        const result = await api('/notifications/summary');
        state.unread = result.data.unread;
        renderChrome();
    } catch (ignored) {
        /* silent: the bell is not critical */
    }
}

/* ------------------------------------------------------------- shared bits */

function listingCard(listing) {
    const visual = listing.photoUrl
        ? `<img src="${esc(listing.photoUrl)}" alt="">`
        : `<span>${CATEGORY_ICON[listing.category] || '📦'}</span>`;
    return `
        <a class="listing-card" href="#/listing/${listing.id}">
            <div class="thumb">${visual}
                <span class="tag badge-pill status-${listing.status}">${statusLabel(listing.status)}</span>
            </div>
            <div class="listing-body">
                <div class="listing-title">${esc(listing.title)}</div>
                <div class="price">${money(listing.price)}</div>
                <div class="listing-meta">
                    <span>${esc(listing.conditionLabel)}</span><span>·</span><span>${esc(listing.categoryLabel)}</span>
                </div>
                <div class="listing-meta"><span>${esc(listing.sellerName)}</span></div>
            </div>
        </a>`;
}

function options(list, selected, placeholder) {
    const head = placeholder ? `<option value="">${esc(placeholder)}</option>` : '';
    return head + list.map((item) => {
        const value = item.value !== undefined ? item.value : item;
        const label = item.label !== undefined ? item.label : item;
        return `<option value="${esc(value)}" ${String(value) === String(selected) ? 'selected' : ''}>${esc(label)}</option>`;
    }).join('');
}

/* ----------------------------------------------------------------- routing */

const ROUTES = [
    { pattern: /^\/home$/, page: 'home', view: homeView },
    { pattern: /^\/listing\/(\d+)$/, page: 'home', view: listingView },
    { pattern: /^\/publish$/, page: 'my-listings', view: publishView },
    { pattern: /^\/edit\/(\d+)$/, page: 'my-listings', view: publishView },
    { pattern: /^\/my-listings$/, page: 'my-listings', view: myListingsView },
    { pattern: /^\/requests$/, page: 'requests', view: requestsView },
    { pattern: /^\/matches$/, page: 'matches', view: matchesView },
    { pattern: /^\/favorites$/, page: 'favorites', view: favoritesView },
    { pattern: /^\/notifications$/, page: 'notifications', view: notificationsView },
    { pattern: /^\/profile$/, page: 'profile', view: profileView },
    { pattern: /^\/login$/, page: 'login', view: loginView },
    { pattern: /^\/register$/, page: 'register', view: registerView }
];

async function render() {
    const raw = (window.location.hash || '#/home').replace(/^#/, '');
    const path = raw.split('?')[0] || '/home';
    const view = document.getElementById('view');
    view.innerHTML = `<div class="empty">Loading…</div>`;

    let matched = null;
    for (const route of ROUTES) {
        const result = route.pattern.exec(path);
        if (result) {
            matched = { route, params: result.slice(1) };
            break;
        }
    }
    if (!matched) {
        view.innerHTML = `<div class="empty"><span class="big">🧭</span><h2>Page not found</h2>
            <a class="btn btn-primary" href="#/home">Back to browse</a></div>`;
        return;
    }
    markNav(matched.route.page);
    try {
        await matched.route.view(...matched.params);
    } catch (error) {
        view.innerHTML = `<div class="empty"><span class="big">⚠️</span><h2>Something went wrong</h2>
            <p>${esc(error.message)}</p><a class="btn" href="#/home">Back to browse</a></div>`;
    }
    window.scrollTo(0, 0);
}

/* -------------------------------------------------------------------- home */

async function homeView() {
    if (!state.meta) {
        state.meta = (await api('/meta')).data;
    }
    const filters = state.search;
    const view = document.getElementById('view');

    const conditionChips = state.meta.conditions.map((condition) => {
        const on = filters.conditions.includes(condition.value);
        return `<span class="chip ${on ? 'on' : ''}" data-condition="${condition.value}">${esc(condition.label)}</span>`;
    }).join('');

    view.innerHTML = `
        <div class="page-head">
            <div>
                <h1>Buy and sell inside MUST</h1>
                <p>Textbooks, electronics, furniture and more — post a request and let the platform find the match.</p>
            </div>
            <a class="btn" href="#/requests">Post a want-to-buy request</a>
        </div>

        <form class="filter-panel" id="filter-form">
            <div class="filter-grid">
                <div class="field">
                    <label for="f-keyword">Keyword</label>
                    <input id="f-keyword" placeholder="Search title or description" value="${esc(filters.keyword)}">
                </div>
                <div class="field">
                    <label for="f-category">Category</label>
                    <select id="f-category">${options(state.meta.categories, filters.category, 'All categories')}</select>
                </div>
                <div class="field">
                    <label for="f-min">Min price</label>
                    <input id="f-min" type="number" min="0" step="1" placeholder="0" value="${esc(filters.min)}">
                </div>
                <div class="field">
                    <label for="f-max">Max price</label>
                    <input id="f-max" type="number" min="0" step="1" placeholder="Any" value="${esc(filters.max)}">
                </div>
                <div class="field">
                    <label for="f-sort">Sort by</label>
                    <select id="f-sort">${options(state.meta.sorts, filters.sort)}</select>
                </div>
            </div>
            <div class="row" style="margin-top:12px">
                <span style="font-size:13px;color:var(--text-soft)">Condition</span>
                <div class="chips" id="condition-chips">${conditionChips}</div>
                <span class="spacer"></span>
                <button class="btn btn-sm" type="button" id="reset-filters">Reset</button>
                <button class="btn btn-primary btn-sm" type="submit">Search</button>
            </div>
        </form>

        <div id="results"><div class="empty">Searching…</div></div>`;

    document.getElementById('condition-chips').addEventListener('click', (event) => {
        const chip = event.target.closest('[data-condition]');
        if (!chip) {
            return;
        }
        const value = chip.dataset.condition;
        filters.conditions = filters.conditions.includes(value)
            ? filters.conditions.filter((item) => item !== value)
            : filters.conditions.concat(value);
        chip.classList.toggle('on');
    });

    document.getElementById('filter-form').addEventListener('submit', (event) => {
        event.preventDefault();
        filters.keyword = document.getElementById('f-keyword').value.trim();
        filters.category = document.getElementById('f-category').value;
        filters.min = document.getElementById('f-min').value;
        filters.max = document.getElementById('f-max').value;
        filters.sort = document.getElementById('f-sort').value;
        filters.page = 0;
        loadResults();
    });

    document.getElementById('reset-filters').addEventListener('click', () => {
        state.search = { keyword: '', category: '', min: '', max: '', conditions: [], sort: 'newest', page: 0 };
        homeView();
    });

    document.getElementById('f-category').addEventListener('change', () => {
        document.getElementById('filter-form').requestSubmit();
    });
    document.getElementById('f-sort').addEventListener('change', () => {
        document.getElementById('filter-form').requestSubmit();
    });

    await loadResults();
}

async function loadResults() {
    const filters = state.search;
    const params = new URLSearchParams();
    if (filters.keyword) params.set('keyword', filters.keyword);
    if (filters.category) params.set('category', filters.category);
    if (filters.min !== '') params.set('minPrice', filters.min);
    if (filters.max !== '') params.set('maxPrice', filters.max);
    if (filters.sort) params.set('sort', filters.sort);
    filters.conditions.forEach((condition) => params.append('condition', condition));
    params.set('page', filters.page);
    params.set('size', '20');

    const container = document.getElementById('results');
    try {
        const result = await api('/search?' + params.toString());
        const page = result.data;
        if (!page.items.length) {
            container.innerHTML = `
                <div class="empty">
                    <span class="big">🔍</span>
                    <h2>No listing matches these filters</h2>
                    <p>Try a shorter keyword, a wider price range or fewer conditions.</p>
                    <button class="btn" id="clear-all">Clear all filters</button>
                </div>`;
            const clear = document.getElementById('clear-all');
            if (clear) {
                clear.addEventListener('click', () => {
                    state.search = { keyword: '', category: '', min: '', max: '', conditions: [], sort: 'newest', page: 0 };
                    homeView();
                });
            }
            return;
        }
        container.innerHTML = `
            <div class="row" style="margin-bottom:12px;color:var(--text-soft);font-size:13.5px">
                <span>${page.total} listing${page.total === 1 ? '' : 's'} available</span>
            </div>
            <div class="grid cards">${page.items.map(listingCard).join('')}</div>
            ${pagination(page)}`;

        container.querySelectorAll('[data-page]').forEach((button) => {
            button.addEventListener('click', () => {
                state.search.page = Number(button.dataset.page);
                loadResults();
            });
        });
    } catch (error) {
        container.innerHTML = `<div class="empty"><span class="big">⚠️</span><p>${esc(error.message)}</p></div>`;
    }
}

function pagination(page) {
    if (page.totalPages <= 1) {
        return '';
    }
    return `
        <div class="pagination">
            <button class="btn btn-sm" data-page="${page.page - 1}" ${page.page === 0 ? 'disabled' : ''}>Previous</button>
            <span>Page ${page.page + 1} of ${page.totalPages}</span>
            <button class="btn btn-sm" data-page="${page.page + 1}" ${page.page + 1 >= page.totalPages ? 'disabled' : ''}>Next</button>
        </div>`;
}

/* ---------------------------------------------------------- listing detail */

async function listingView(id) {
    if (!state.meta) {
        state.meta = (await api('/meta')).data;
    }
    const detail = (await api('/listings/' + id)).data;
    const listing = detail.listing;
    const view = document.getElementById('view');

    const history = detail.history.length
        ? `<ul class="timeline">${detail.history.map((item) => `
            <li><span class="dot"></span><span>${item.fromStatus ? statusLabel(item.fromStatus) + ' → ' : 'Created as '}${statusLabel(item.toStatus)}
            <span style="color:var(--text-faint)">· ${dateStr(item.changedAt)}</span></span></li>`).join('')}</ul>`
        : `<p style="color:var(--text-faint);font-size:13px">No status change yet.</p>`;

    const ownerActions = detail.owner ? `
        <div class="card tight" style="margin-top:14px">
            <h3>Seller actions</h3>
            <div class="row">
                ${listing.status === 'AVAILABLE' ? `<button class="btn btn-sm" id="act-reserve">Mark as reserved</button>` : ''}
                ${listing.status === 'RESERVED' ? `<button class="btn btn-sm" id="act-sold">Mark as sold</button>
                     <button class="btn btn-sm" id="act-available">Back to available</button>` : ''}
                ${listing.status !== 'SOLD' ? `<a class="btn btn-sm" href="#/edit/${listing.id}">Edit</a>` : ''}
                <button class="btn btn-sm btn-danger" id="act-delete">Delete</button>
            </div>
            <p class="hint" style="margin:8px 0 0">Available → Reserved → Sold. Sold listings cannot be edited or restored in version 1.</p>
        </div>` : '';

    const favoriteButton = state.user && !detail.owner
        ? `<button class="btn ${detail.favorite ? 'btn-primary' : ''}" id="fav-btn">${detail.favorite ? '★ Saved' : '☆ Save'}</button>`
        : '';

    view.innerHTML = `
        <div class="row" style="margin-bottom:14px">
            <a class="btn btn-sm btn-ghost" href="#/home">← Back to browse</a>
        </div>
        <div class="grid split">
            <div class="card">
                <div class="row" style="align-items:center;gap:8px;margin-bottom:10px">
                    <span class="badge-pill status-${listing.status}">${statusLabel(listing.status)}</span>
                    <span class="badge-pill badge-soft">${esc(listing.conditionLabel)}</span>
                    <span class="badge-pill badge-soft">${esc(listing.categoryLabel)}</span>
                </div>
                <div class="detail-head">
                    <div>
                        <h1 style="margin-bottom:6px">${esc(listing.title)}</h1>
                        <div class="listing-meta">Listed ${dayStr(listing.createdAt)} · ${esc(listing.sellerName)}</div>
                    </div>
                    <div class="detail-price">${money(listing.price)}</div>
                </div>
                <div class="thumb" style="aspect-ratio:16/7;margin:16px 0;border-radius:12px">
                    ${listing.photoUrl ? `<img src="${esc(listing.photoUrl)}" alt="">` : `<span style="font-size:56px">${CATEGORY_ICON[listing.category] || '📦'}</span>`}
                </div>
                <h3>Description</h3>
                <p class="detail-body">${esc(listing.description)}</p>
                <div class="row" style="margin-top:16px">${favoriteButton}</div>
                ${ownerActions}
            </div>

            <div class="stack">
                <div class="card tight">
                    <h3>Item details</h3>
                    <dl class="kv">
                        <dt>Price</dt><dd>${money(listing.price)}</dd>
                        <dt>Condition</dt><dd>${esc(listing.conditionLabel)}</dd>
                        <dt>Category</dt><dd>${esc(listing.categoryLabel)}</dd>
                        <dt>Status</dt><dd>${statusLabel(listing.status)}</dd>
                        <dt>Seller</dt><dd>${esc(listing.sellerName)}</dd>
                        <dt>Favorited by</dt><dd>${listing.favoriteCount} student${listing.favoriteCount === 1 ? '' : 's'}</dd>
                        <dt>Last update</dt><dd>${dateStr(listing.updatedAt)}</dd>
                    </dl>
                </div>
                <div class="card tight">
                    <h3>Status history</h3>
                    ${history}
                </div>
            </div>
        </div>`;

    const favButton = document.getElementById('fav-btn');
    if (favButton) {
        favButton.addEventListener('click', async () => {
            try {
                if (detail.favorite) {
                    await api('/favorites/' + listing.id, { method: 'DELETE' });
                    toast('Removed from favorites');
                } else {
                    await api('/favorites/' + listing.id, { method: 'POST' });
                    toast('Saved to favorites');
                }
                listingView(id);
            } catch (error) {
                toast(error.message, true);
            }
        });
    }

    const act = (id, body, message) => {
        const button = document.getElementById(id);
        if (!button) {
            return;
        }
        button.addEventListener('click', async () => {
            try {
                await api('/listings/' + listing.id + '/status', { method: 'PUT', body: { status: body } });
                toast(message);
                listingView(id);
                refreshUnread();
            } catch (error) {
                toast(error.message, true);
            }
        });
    };
    act('act-reserve', 'RESERVED', 'Listing reserved, matched buyers were notified');
    act('act-sold', 'SOLD', 'Listing marked as sold');
    act('act-available', 'AVAILABLE', 'Listing is available again');

    const deleteButton = document.getElementById('act-delete');
    if (deleteButton) {
        deleteButton.addEventListener('click', async () => {
            if (!window.confirm('Delete this listing together with its favorites and matches?')) {
                return;
            }
            try {
                await api('/listings/' + listing.id, { method: 'DELETE' });
                toast('Listing deleted');
                go('#/my-listings');
            } catch (error) {
                toast(error.message, true);
            }
        });
    }
}

/* --------------------------------------------------------- publish / edit */

async function publishView(editId) {
    if (!requireLogin()) {
        return;
    }
    if (!state.meta) {
        state.meta = (await api('/meta')).data;
    }
    let existing = null;
    if (editId) {
        const detail = (await api('/listings/' + editId)).data;
        if (!detail.owner) {
            document.getElementById('view').innerHTML = `<div class="empty"><span class="big">⛔</span>
                <h2>You cannot edit this listing</h2><a class="btn" href="#/home">Back to browse</a></div>`;
            return;
        }
        existing = detail.listing;
    }

    document.getElementById('view').innerHTML = `
        <div class="page-head">
            <div>
                <h1>${existing ? 'Edit listing' : 'Sell an item'}</h1>
                <p>Five fields, three clicks: title, category, condition and price are enough to publish.</p>
            </div>
            <a class="btn" href="#/my-listings">My listings</a>
        </div>
        <form class="card" id="listing-form" style="max-width:660px">
            <div class="field">
                <label for="l-title">Title</label>
                <input id="l-title" maxlength="100" required placeholder="e.g. 24-inch IPS monitor 75Hz HDMI" value="${esc(existing ? existing.title : '')}">
            </div>
            <div class="field-row">
                <div class="field">
                    <label for="l-category">Category</label>
                    <select id="l-category">${options(state.meta.categories, existing ? existing.category : '')}</select>
                </div>
                <div class="field">
                    <label for="l-condition">Condition</label>
                    <select id="l-condition">${options(state.meta.conditions, existing ? existing.condition : 'GOOD')}</select>
                </div>
            </div>
            <div class="field">
                <label for="l-price">Asking price (MOP)</label>
                <input id="l-price" type="number" min="0.01" step="0.01" required value="${existing ? existing.price : ''}">
            </div>
            <div class="field">
                <label for="l-description">Description</label>
                <textarea id="l-description" maxlength="2000" required placeholder="Age, condition details, what is included, pick-up place…">${esc(existing ? existing.description : '')}</textarea>
            </div>
            <div class="field">
                <label for="l-photo">Photo URL (optional)</label>
                <input id="l-photo" maxlength="300" placeholder="https://…" value="${esc(existing && existing.photoUrl ? existing.photoUrl : '')}">
                <span class="hint">Version 1 accepts one photo per listing.</span>
            </div>
            <div class="row">
                <button class="btn btn-primary" type="submit">${existing ? 'Save changes' : 'Publish listing'}</button>
                <button class="btn" type="button" id="cancel-btn">Cancel</button>
            </div>
        </form>`;

    document.getElementById('cancel-btn').addEventListener('click', () => go(existing ? '#/listing/' + existing.id : '#/home'));

    document.getElementById('listing-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const body = {
            title: document.getElementById('l-title').value.trim(),
            category: document.getElementById('l-category').value,
            condition: document.getElementById('l-condition').value,
            price: Number(document.getElementById('l-price').value),
            description: document.getElementById('l-description').value.trim(),
            photoUrl: document.getElementById('l-photo').value.trim() || null
        };
        try {
            const result = existing
                ? await api('/listings/' + existing.id, { method: 'PUT', body })
                : await api('/listings', { method: 'POST', body });
            toast(existing ? 'Listing updated' : 'Listing published, matching started');
            refreshUnread();
            go('#/listing/' + result.data.id);
        } catch (error) {
            toast(error.message, true);
        }
    });
}

/* ------------------------------------------------------------- my listings */

async function myListingsView() {
    if (!requireLogin()) {
        return;
    }
    const listings = (await api('/my/listings')).data;
    const view = document.getElementById('view');

    const rows = listings.map((listing) => `
        <tr>
            <td><a href="#/listing/${listing.id}">${esc(listing.title)}</a>
                <div class="listing-meta">${esc(listing.categoryLabel)} · ${esc(listing.conditionLabel)}</div></td>
            <td>${money(listing.price)}</td>
            <td><span class="badge-pill status-${listing.status}">${statusLabel(listing.status)}</span></td>
            <td>${listing.favoriteCount}</td>
            <td>${dayStr(listing.createdAt)}</td>
            <td class="row" style="gap:6px">
                ${listing.status !== 'SOLD' ? `<a class="btn btn-sm" href="#/edit/${listing.id}">Edit</a>` : ''}
                ${listing.status === 'AVAILABLE' ? `<button class="btn btn-sm" data-reserve="${listing.id}">Reserve</button>` : ''}
                ${listing.status === 'RESERVED' ? `<button class="btn btn-sm" data-sold="${listing.id}">Sold</button>` : ''}
                <button class="btn btn-sm btn-danger" data-delete="${listing.id}">Delete</button>
            </td>
        </tr>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>My listings</h1><p>${listings.length} listing${listings.length === 1 ? '' : 's'} in total. Active listings per user are limited to 50.</p></div>
            <a class="btn btn-primary" href="#/publish">+ New listing</a>
        </div>
        ${listings.length ? `
        <div class="card" style="padding:6px 14px">
            <table>
                <thead><tr><th>Item</th><th>Price</th><th>Status</th><th>Favorites</th><th>Published</th><th></th></tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>` : `<div class="empty"><span class="big">📦</span><h2>No listings yet</h2>
            <p>Publish your first item in under a minute.</p><a class="btn btn-primary" href="#/publish">Sell an item</a></div>`}`;

    const change = async (id, status, message) => {
        try {
            await api('/listings/' + id + '/status', { method: 'PUT', body: { status } });
            toast(message);
            myListingsView();
        } catch (error) {
            toast(error.message, true);
        }
    };
    view.querySelectorAll('[data-reserve]').forEach((button) => {
        button.addEventListener('click', () => change(button.dataset.reserve, 'RESERVED', 'Listing reserved'));
    });
    view.querySelectorAll('[data-sold]').forEach((button) => {
        button.addEventListener('click', () => change(button.dataset.sold, 'SOLD', 'Listing marked as sold'));
    });
    view.querySelectorAll('[data-delete]').forEach((button) => {
        button.addEventListener('click', async () => {
            if (!window.confirm('Delete this listing and its favorites and matches?')) {
                return;
            }
            try {
                await api('/listings/' + button.dataset.delete, { method: 'DELETE' });
                toast('Listing deleted');
                myListingsView();
            } catch (error) {
                toast(error.message, true);
            }
        });
    });
}

/* --------------------------------------------------------------- requests */

async function requestsView() {
    if (!requireLogin()) {
        return;
    }
    if (!state.meta) {
        state.meta = (await api('/meta')).data;
    }
    const requests = (await api('/my/requests')).data;
    const view = document.getElementById('view');

    const list = requests.map((request) => `
        <div class="card tight">
            <div class="row" style="justify-content:space-between;align-items:flex-start">
                <div>
                    <div class="row" style="gap:8px">
                        <strong>${esc(request.keywords)}</strong>
                        <span class="badge-pill ${request.status === 'ACTIVE' ? 'status-AVAILABLE' : 'status-SOLD'}">${request.status === 'ACTIVE' ? 'Active' : 'Closed'}</span>
                    </div>
                    <div class="listing-meta" style="margin-top:4px">
                        ${esc(request.categoryLabel)} · Budget ${money(request.minBudget)} – ${money(request.maxBudget)}
                        · Acceptable condition: ${esc(request.acceptableConditionLabel)}
                    </div>
                    ${request.requirements ? `<div class="listing-meta" style="margin-top:4px">${esc(request.requirements)}</div>` : ''}
                </div>
                ${request.status === 'ACTIVE' ? `<button class="btn btn-sm btn-danger" data-close="${request.id}">Close request</button>` : ''}
            </div>
        </div>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>Want-to-buy requests</h1>
                <p>Post what you need; the matcher compares it with every active listing within seconds.</p></div>
        </div>
        <div class="grid split">
            <div class="stack">
                <h2>My requests (${requests.length}/20)</h2>
                ${requests.length ? list : `<div class="empty"><span class="big">📝</span><p>No request yet. Describe the item you are looking for.</p></div>`}
            </div>
            <form class="card" id="request-form">
                <h3>New request</h3>
                <div class="field">
                    <label for="r-keywords">What are you looking for?</label>
                    <input id="r-keywords" maxlength="200" required placeholder="e.g. monitor 24 inch hdmi ips">
                </div>
                <div class="field">
                    <label for="r-category">Category</label>
                    <select id="r-category">${options(state.meta.categories, '')}</select>
                </div>
                <div class="field-row">
                    <div class="field">
                        <label for="r-min">Min budget</label>
                        <input id="r-min" type="number" min="0.01" step="0.01" required>
                    </div>
                    <div class="field">
                        <label for="r-max">Max budget</label>
                        <input id="r-max" type="number" min="0.01" step="0.01" required>
                    </div>
                </div>
                <div class="field">
                    <label for="r-condition">Acceptable condition</label>
                    <select id="r-condition">${options(state.meta.conditions, 'GOOD')}</select>
                </div>
                <div class="field">
                    <label for="r-requirements">Other requirements (optional)</label>
                    <textarea id="r-requirements" maxlength="500" style="min-height:70px"></textarea>
                </div>
                <button class="btn btn-primary btn-block" type="submit">Publish request</button>
            </form>
        </div>`;

    view.querySelectorAll('[data-close]').forEach((button) => {
        button.addEventListener('click', async () => {
            try {
                await api('/requests/' + button.dataset.close + '/close', { method: 'POST' });
                toast('Request closed');
                requestsView();
            } catch (error) {
                toast(error.message, true);
            }
        });
    });

    document.getElementById('request-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        const body = {
            keywords: document.getElementById('r-keywords').value.trim(),
            category: document.getElementById('r-category').value,
            minBudget: Number(document.getElementById('r-min').value),
            maxBudget: Number(document.getElementById('r-max').value),
            acceptableCondition: document.getElementById('r-condition').value,
            requirements: document.getElementById('r-requirements').value.trim() || null
        };
        try {
            await api('/requests', { method: 'POST', body });
            toast('Request published, matching started');
            refreshUnread();
            requestsView();
        } catch (error) {
            toast(error.message, true);
        }
    });
}

/* ---------------------------------------------------------------- matches */

async function matchesView() {
    if (!requireLogin()) {
        return;
    }
    const matches = (await api('/matches')).data;
    const view = document.getElementById('view');

    const cards = matches.map((match) => {
        const listing = match.listing;
        const request = match.request;
        const bar = (label, value) => `
            <div class="score-bar">
                <span>${label}</span>
                <div class="bar"><span style="width:${Math.round(value * 100)}%"></span></div>
                <span>${value.toFixed(2)}</span>
            </div>`;
        return `
            <div class="card">
                <div class="match-card">
                    <div class="score-ring">${match.score.toFixed(2)}</div>
                    <div>
                        <div class="row" style="gap:8px">
                            <span class="badge-pill badge-accent">${match.myRole === 'SELLER' ? 'Your item · buyer found' : 'Your request · item found'}</span>
                            <span class="badge-pill badge-soft">Matched ${dayStr(match.createdAt)}</span>
                        </div>
                        <h3 style="margin:8px 0 2px">
                            <a href="#/listing/${listing.id}">${esc(listing.title)}</a>
                            <span style="font-weight:600;color:var(--text-soft);font-size:14px">· ${money(listing.price)}</span>
                        </h3>
                        <div class="listing-meta">
                            ${esc(listing.conditionLabel)} · ${esc(listing.categoryLabel)} · seller ${esc(listing.sellerName)}
                        </div>
                        <div class="listing-meta" style="margin-top:6px">
                            Request: “${esc(request.keywords)}” · budget ${money(request.minBudget)} – ${money(request.maxBudget)}
                            · buyer ${esc(request.buyerName)}
                        </div>
                        <div class="score-bars">
                            ${bar('Category 0.40', match.categoryScore)}
                            ${bar('Price 0.35', match.priceScore)}
                            ${bar('Condition 0.25', match.conditionScore)}
                            ${bar('Keyword (gate)', match.keywordScore)}
                        </div>
                    </div>
                </div>
            </div>`;
    }).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>Matches</h1>
                <p>Open matches only, ranked by score. Score = 0.40 × category + 0.35 × price + 0.25 × condition; pairs above 0.70 are shown.</p></div>
            <a class="btn" href="#/requests">My requests</a>
        </div>
        ${matches.length ? `<div class="stack">${cards}</div>` : `
            <div class="empty">
                <span class="big">🤝</span>
                <h2>No open match yet</h2>
                <p>Publish a listing or a want-to-buy request. The matcher runs automatically after every change.</p>
                <a class="btn btn-primary" href="#/publish">Sell an item</a>
                <a class="btn" href="#/requests">Post a request</a>
            </div>`}`;
}

/* -------------------------------------------------------------- favorites */

async function favoritesView() {
    if (!requireLogin()) {
        return;
    }
    const favorites = (await api('/favorites')).data;
    const view = document.getElementById('view');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>Favorites</h1><p>You are notified when a saved item changes price or status.</p></div>
        </div>
        ${favorites.length
            ? `<div class="grid cards">${favorites.map(listingCard).join('')}</div>`
            : `<div class="empty"><span class="big">☆</span><h2>No saved item</h2>
               <p>Use the Save button on any listing to keep it here.</p>
               <a class="btn btn-primary" href="#/home">Browse listings</a></div>`}`;
}

/* ----------------------------------------------------------- notifications */

async function notificationsView() {
    if (!requireLogin()) {
        return;
    }
    const page = (await api('/notifications?page=0&size=30')).data;
    const view = document.getElementById('view');

    const items = page.items.map((item) => `
        <div class="card tight" style="border-left:3px solid ${item.read ? 'var(--border)' : 'var(--accent)'}">
            <div class="row" style="justify-content:space-between;align-items:flex-start">
                <div>
                    <div class="row" style="gap:8px">
                        <strong>${esc(item.title)}</strong>
                        ${item.read ? '' : '<span class="badge-pill badge-accent">New</span>'}
                    </div>
                    <div style="color:var(--text-soft);font-size:13.5px;margin-top:4px">${esc(item.body)}</div>
                    <div class="listing-meta" style="margin-top:6px">
                        ${dateStr(item.createdAt)}
                        ${item.listingId ? `· <a href="#/listing/${item.listingId}">Open listing</a>` : ''}
                    </div>
                </div>
                ${item.read ? '' : `<button class="btn btn-sm btn-ghost" data-read="${item.id}">Mark read</button>`}
            </div>
        </div>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>Notifications</h1><p>Matches, status changes and price changes on saved items. Kept for 90 days.</p></div>
            <button class="btn" id="read-all">Mark all as read</button>
        </div>
        ${page.items.length ? `<div class="stack">${items}</div>` : `<div class="empty"><span class="big">🔔</span>
            <h2>Nothing yet</h2><p>Notifications appear here as soon as a match is found.</p></div>`}`;

    const readAll = document.getElementById('read-all');
    if (readAll) {
        readAll.addEventListener('click', async () => {
            await api('/notifications/read-all', { method: 'POST' });
            state.unread = 0;
            renderChrome();
            notificationsView();
        });
    }
    view.querySelectorAll('[data-read]').forEach((button) => {
        button.addEventListener('click', async () => {
            await api('/notifications/' + button.dataset.read + '/read', { method: 'POST' });
            refreshUnread();
            notificationsView();
        });
    });
}

/* ----------------------------------------------------------------- profile */

async function profileView() {
    if (!requireLogin()) {
        return;
    }
    const user = (await api('/auth/me')).data;
    const view = document.getElementById('view');

    view.innerHTML = `
        <div class="page-head"><div><h1>Profile &amp; settings</h1>
            <p>Personal data is visible only to you; other students see your display name on listings.</p></div></div>
        <div class="grid split">
            <div class="stack">
                <form class="card" id="profile-form">
                    <h3>Account</h3>
                    <div class="field">
                        <label for="p-name">Display name</label>
                        <input id="p-name" value="${esc(user.name)}" maxlength="60" required>
                    </div>
                    <div class="field">
                        <label for="p-contact">Contact (phone, WeChat…)</label>
                        <input id="p-contact" value="${esc(user.contact || '')}" maxlength="120">
                    </div>
                    <dl class="kv" style="margin:6px 0 16px">
                        <dt>Student number</dt><dd>${esc(user.studentNo)}</dd>
                        <dt>E-mail</dt><dd>${esc(user.email)}</dd>
                        <dt>Member since</dt><dd>${dayStr(user.createdAt)}</dd>
                    </dl>
                    <button class="btn btn-primary" type="submit">Save profile</button>
                </form>

                <form class="card" id="password-form">
                    <h3>Change password</h3>
                    <div class="field">
                        <label for="p-old">Current password</label>
                        <input id="p-old" type="password" required>
                    </div>
                    <div class="field">
                        <label for="p-new">New password (at least 8 characters)</label>
                        <input id="p-new" type="password" minlength="8" required>
                    </div>
                    <button class="btn" type="submit">Update password</button>
                </form>
            </div>
            <div class="card tight">
                <h3>Delete account</h3>
                <p style="font-size:13.5px;color:var(--text-soft)">
                    Deleting the account also removes your listings, requests, favorites, matches and notifications.
                    This cannot be undone.
                </p>
                <button class="btn btn-danger btn-block" id="delete-account">Delete my account</button>
            </div>
        </div>`;

    document.getElementById('profile-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            const result = await api('/me', {
                method: 'PUT',
                body: {
                    name: document.getElementById('p-name').value.trim(),
                    contact: document.getElementById('p-contact').value.trim() || null
                }
            });
            state.user = result.data;
            renderChrome();
            toast('Profile updated');
        } catch (error) {
            toast(error.message, true);
        }
    });

    document.getElementById('password-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            await api('/me/password', {
                method: 'PUT',
                body: {
                    oldPassword: document.getElementById('p-old').value,
                    newPassword: document.getElementById('p-new').value
                }
            });
            toast('Password changed');
            event.target.reset();
        } catch (error) {
            toast(error.message, true);
        }
    });

    document.getElementById('delete-account').addEventListener('click', async () => {
        if (!window.confirm('Delete your account and all related data? This cannot be undone.')) {
            return;
        }
        try {
            await api('/me', { method: 'DELETE' });
            state.user = null;
            state.unread = 0;
            renderChrome();
            toast('Account deleted');
            go('#/home');
        } catch (error) {
            toast(error.message, true);
        }
    });
}

/* -------------------------------------------------------------- auth pages */

function loginView() {
    document.getElementById('view').innerHTML = `
        <div class="auth-wrap">
            <div class="card">
                <h2>Log in</h2>
                <p style="color:var(--text-soft);font-size:13.5px">Use your e-mail or your student number.</p>
                <form id="login-form">
                    <div class="field">
                        <label for="login-id">E-mail or student number</label>
                        <input id="login-id" required autocomplete="username">
                    </div>
                    <div class="field">
                        <label for="login-password">Password</label>
                        <input id="login-password" type="password" required autocomplete="current-password">
                    </div>
                    <button class="btn btn-primary btn-block" type="submit">Log in</button>
                </form>
                <p style="margin-top:14px;font-size:13.5px">No account yet? <a href="#/register">Register</a></p>
                <div class="demo-hint">
                    <strong>Demo account</strong><br>
                    Student number <code>1240016186</code> · password <code>Password123</code><br>
                    Buyer demo: <code>1240008865</code> (has an active monitor request).
                </div>
            </div>
        </div>`;

    document.getElementById('login-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            const result = await api('/auth/login', {
                method: 'POST',
                body: {
                    identifier: document.getElementById('login-id').value.trim(),
                    password: document.getElementById('login-password').value
                }
            });
            state.user = result.data;
            renderChrome();
            await refreshUnread();
            toast('Welcome back, ' + state.user.name.split(' ')[0]);
            go('#/home');
        } catch (error) {
            toast(error.message, true);
        }
    });
}

function registerView() {
    document.getElementById('view').innerHTML = `
        <div class="auth-wrap">
            <div class="card">
                <h2>Create an account</h2>
                <p style="color:var(--text-soft);font-size:13.5px">MUST students only. Five fields, less than two minutes.</p>
                <form id="register-form">
                    <div class="field">
                        <label for="reg-name">Name</label>
                        <input id="reg-name" maxlength="60" required>
                    </div>
                    <div class="field">
                        <label for="reg-no">Student number</label>
                        <input id="reg-no" inputmode="numeric" pattern="[0-9]{6,20}" required>
                    </div>
                    <div class="field">
                        <label for="reg-email">E-mail</label>
                        <input id="reg-email" type="email" maxlength="120" required>
                    </div>
                    <div class="field-row">
                        <div class="field">
                            <label for="reg-password">Password</label>
                            <input id="reg-password" type="password" minlength="8" required>
                        </div>
                        <div class="field">
                            <label for="reg-confirm">Repeat password</label>
                            <input id="reg-confirm" type="password" minlength="8" required>
                        </div>
                    </div>
                    <button class="btn btn-primary btn-block" type="submit">Register</button>
                </form>
                <p style="margin-top:14px;font-size:13.5px">Already registered? <a href="#/login">Log in</a></p>
            </div>
        </div>`;

    document.getElementById('register-form').addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
            const result = await api('/auth/register', {
                method: 'POST',
                body: {
                    name: document.getElementById('reg-name').value.trim(),
                    studentNo: document.getElementById('reg-no').value.trim(),
                    email: document.getElementById('reg-email').value.trim(),
                    password: document.getElementById('reg-password').value,
                    confirmPassword: document.getElementById('reg-confirm').value
                }
            });
            state.user = result.data;
            renderChrome();
            await refreshUnread();
            toast('Welcome to Campus Loop');
            go('#/home');
        } catch (error) {
            toast(error.message, true);
        }
    });
}

/* -------------------------------------------------------------------- boot */

async function boot() {
    document.getElementById('publish-btn').addEventListener('click', () => {
        go(state.user ? '#/publish' : '#/login');
    });
    document.addEventListener('click', (event) => {
        const menu = document.getElementById('account-menu');
        if (menu && !event.target.closest('.account')) {
            menu.classList.remove('open');
        }
    });
    window.addEventListener('hashchange', render);

    try {
        const result = await api('/auth/me');
        state.user = result.data;
    } catch (ignored) {
        state.user = null;
    }
    renderChrome();
    await refreshUnread();
    await render();
}

boot();

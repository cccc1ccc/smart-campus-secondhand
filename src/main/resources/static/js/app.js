/* Campus Loop - single page frontend for the Smart Campus Second-hand Trading Platform.
   Plain JavaScript, no build step: every page calls the REST API with the session cookie.
   All visible text goes through t() from i18n.js, so EN / 中文 can be switched at any time. */

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

const statusLabel = (status) => t('state.' + status);
const categoryLabel = (value) => t('cat.' + value);
const conditionLabel = (value) => t('cond.' + value);

const dateStr = (iso) => iso
    ? new Date(iso).toLocaleString(localeCode(), {
        day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    })
    : '';

const dayStr = (iso) => iso
    ? new Date(iso).toLocaleDateString(localeCode(), { day: '2-digit', month: 'short', year: 'numeric' })
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
        const raw = (payload && payload.message) || 'HTTP ' + response.status;
        throw new Error(serverMessage(raw));
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
            <h2>${t('common.pleaseLogin')}</h2>
            <p>${t('common.loginRequired')}</p>
            <a class="btn btn-primary" href="#/login">${t('common.login')}</a>
            <a class="btn" href="#/register">${t('common.createAccount')}</a>
        </div>`;
    return false;
}

/* -------------------------------------------------------------- chrome/nav */

/** Translates the static shell (header, footer, document title). */
function applyStaticTranslations() {
    document.documentElement.lang = getLang() === 'zh' ? 'zh-CN' : 'en';
    document.title = t('app.title');
    document.querySelectorAll('[data-i18n]').forEach((node) => {
        node.textContent = t(node.dataset.i18n);
    });
    document.querySelectorAll('[data-i18n-title]').forEach((node) => {
        node.title = t(node.dataset.i18nTitle);
    });
    document.querySelectorAll('#lang-switch button').forEach((button) => {
        button.classList.toggle('on', button.dataset.lang === getLang());
    });
}

function renderChrome() {
    const account = document.getElementById('account');
    if (!state.user) {
        account.innerHTML = `<a class="btn btn-sm" href="#/login">${t('common.login')}</a>`;
    } else {
        const initials = state.user.name.split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase();
        const displayName = getLang() === 'zh' ? state.user.name : state.user.name.split(' ')[0];
        account.innerHTML = `
            <button class="account-btn" id="account-toggle">
                <span class="avatar">${esc(initials)}</span>${esc(displayName)}
            </button>
            <div class="menu" id="account-menu">
                <a href="#/profile">${t('menu.profile')}</a>
                <a href="#/my-listings">${t('menu.myListings')}</a>
                <a href="#/requests">${t('menu.myRequests')}</a>
                <div class="divider"></div>
                <button type="button" id="logout-btn">${t('menu.logout')}</button>
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
            toast(t('toast.loggedOut'));
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

function categoryOptions(selected, placeholderKey) {
    const head = placeholderKey ? `<option value="">${t(placeholderKey)}</option>` : '';
    return head + (state.meta ? state.meta.categories : []).map((item) =>
        `<option value="${esc(item.value)}" ${item.value === selected ? 'selected' : ''}>${esc(categoryLabel(item.value))}</option>`
    ).join('');
}

function conditionOptions(selected) {
    return (state.meta ? state.meta.conditions : []).map((item) =>
        `<option value="${esc(item.value)}" ${item.value === selected ? 'selected' : ''}>${esc(conditionLabel(item.value))}</option>`
    ).join('');
}

function sortOptions(selected) {
    const keys = ['newest', 'price_asc', 'price_desc'];
    return keys.map((key) => {
        const label = key === 'newest' ? t('sort.newest') : key === 'price_asc' ? t('sort.priceAsc') : t('sort.priceDesc');
        return `<option value="${key}" ${key === selected ? 'selected' : ''}>${label}</option>`;
    }).join('');
}

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
                    <span>${esc(conditionLabel(listing.condition))}</span><span>·</span>
                    <span>${esc(categoryLabel(listing.category))}</span>
                </div>
                <div class="listing-meta"><span>${esc(listing.sellerName)}</span></div>
            </div>
        </a>`;
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
    view.innerHTML = `<div class="empty">${t('common.loading')}</div>`;

    let matched = null;
    for (const route of ROUTES) {
        const result = route.pattern.exec(path);
        if (result) {
            matched = { route, params: result.slice(1) };
            break;
        }
    }
    if (!matched) {
        view.innerHTML = `<div class="empty"><span class="big">🧭</span><h2>${t('common.pageNotFound')}</h2>
            <a class="btn btn-primary" href="#/home">${t('common.backToBrowse')}</a></div>`;
        return;
    }
    markNav(matched.route.page);
    try {
        await matched.route.view(...matched.params);
    } catch (error) {
        view.innerHTML = `<div class="empty"><span class="big">⚠️</span><h2>${t('common.somethingWrong')}</h2>
            <p>${esc(error.message)}</p><a class="btn" href="#/home">${t('common.backToBrowse')}</a></div>`;
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

    const conditionChips = (state.meta.conditions || []).map((condition) => {
        const on = filters.conditions.includes(condition.value);
        return `<span class="chip ${on ? 'on' : ''}" data-condition="${condition.value}">${esc(conditionLabel(condition.value))}</span>`;
    }).join('');

    view.innerHTML = `
        <div class="page-head">
            <div>
                <h1>${t('home.title')}</h1>
                <p>${t('home.subtitle')}</p>
            </div>
            <a class="btn" href="#/requests">${t('home.postRequest')}</a>
        </div>

        <form class="filter-panel" id="filter-form">
            <div class="filter-grid">
                <div class="field">
                    <label for="f-keyword">${t('filter.keyword')}</label>
                    <input id="f-keyword" placeholder="${t('filter.keywordPh')}" value="${esc(filters.keyword)}">
                </div>
                <div class="field">
                    <label for="f-category">${t('filter.category')}</label>
                    <select id="f-category">${categoryOptions(filters.category, 'filter.allCategories')}</select>
                </div>
                <div class="field">
                    <label for="f-min">${t('filter.minPrice')}</label>
                    <input id="f-min" type="number" min="0" step="1" placeholder="${t('filter.zero')}" value="${esc(filters.min)}">
                </div>
                <div class="field">
                    <label for="f-max">${t('filter.maxPrice')}</label>
                    <input id="f-max" type="number" min="0" step="1" placeholder="${t('filter.anyPrice')}" value="${esc(filters.max)}">
                </div>
                <div class="field">
                    <label for="f-sort">${t('filter.sort')}</label>
                    <select id="f-sort">${sortOptions(filters.sort)}</select>
                </div>
            </div>
            <div class="row" style="margin-top:12px">
                <span style="font-size:13px;color:var(--text-soft)">${t('filter.condition')}</span>
                <div class="chips" id="condition-chips">${conditionChips}</div>
                <span class="spacer"></span>
                <button class="btn btn-sm" type="button" id="reset-filters">${t('filter.reset')}</button>
                <button class="btn btn-primary btn-sm" type="submit">${t('filter.search')}</button>
            </div>
        </form>

        <div id="results"><div class="empty">${t('home.searching')}</div></div>`;

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
                    <h2>${t('home.emptyTitle')}</h2>
                    <p>${t('home.emptyHint')}</p>
                    <button class="btn" id="clear-all">${t('home.clearFilters')}</button>
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
                <span>${t(page.total === 1 ? 'home.countOne' : 'home.countMany', { n: page.total })}</span>
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
            <button class="btn btn-sm" data-page="${page.page - 1}" ${page.page === 0 ? 'disabled' : ''}>${t('common.previous')}</button>
            <span>${t('common.pageOf', { page: page.page + 1, total: page.totalPages })}</span>
            <button class="btn btn-sm" data-page="${page.page + 1}" ${page.page + 1 >= page.totalPages ? 'disabled' : ''}>${t('common.next')}</button>
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
            <li><span class="dot"></span><span>${item.fromStatus
                ? statusLabel(item.fromStatus) + ' → '
                : t('detail.createdAs', { status: statusLabel(item.toStatus) })}${item.fromStatus ? statusLabel(item.toStatus) : ''}
            <span style="color:var(--text-faint)">· ${dateStr(item.changedAt)}</span></span></li>`).join('')}</ul>`
        : `<p style="color:var(--text-faint);font-size:13px">${t('detail.noHistory')}</p>`;

    const ownerActions = detail.owner ? `
        <div class="card tight" style="margin-top:14px">
            <h3>${t('detail.sellerActions')}</h3>
            <div class="row">
                ${listing.status === 'AVAILABLE' ? `<button class="btn btn-sm" id="act-reserve">${t('detail.reserve')}</button>` : ''}
                ${listing.status === 'RESERVED' ? `<button class="btn btn-sm" id="act-sold">${t('detail.markSold')}</button>
                     <button class="btn btn-sm" id="act-available">${t('detail.markAvailable')}</button>` : ''}
                ${listing.status !== 'SOLD' ? `<a class="btn btn-sm" href="#/edit/${listing.id}">${t('common.edit')}</a>` : ''}
                <button class="btn btn-sm btn-danger" id="act-delete">${t('common.delete')}</button>
            </div>
            <p class="hint" style="margin:8px 0 0">${t('detail.statusHint')}</p>
        </div>` : '';

    const favoriteButton = state.user && !detail.owner
        ? `<button class="btn ${detail.favorite ? 'btn-primary' : ''}" id="fav-btn">${detail.favorite ? t('common.saveState') : t('common.unsaved')}</button>`
        : '';

    view.innerHTML = `
        <div class="row" style="margin-bottom:14px">
            <a class="btn btn-sm btn-ghost" href="#/home">${t('common.backToBrowse')}</a>
        </div>
        <div class="grid split">
            <div class="card">
                <div class="row" style="align-items:center;gap:8px;margin-bottom:10px">
                    <span class="badge-pill status-${listing.status}">${statusLabel(listing.status)}</span>
                    <span class="badge-pill badge-soft">${esc(conditionLabel(listing.condition))}</span>
                    <span class="badge-pill badge-soft">${esc(categoryLabel(listing.category))}</span>
                </div>
                <div class="detail-head">
                    <div>
                        <h1 style="margin-bottom:6px">${esc(listing.title)}</h1>
                        <div class="listing-meta">${t('detail.listedOn', { date: dayStr(listing.createdAt), seller: listing.sellerName })}</div>
                    </div>
                    <div class="detail-price">${money(listing.price)}</div>
                </div>
                <div class="thumb" style="aspect-ratio:16/7;margin:16px 0;border-radius:12px">
                    ${listing.photoUrl ? `<img src="${esc(listing.photoUrl)}" alt="">` : `<span style="font-size:56px">${CATEGORY_ICON[listing.category] || '📦'}</span>`}
                </div>
                <h3>${t('detail.description')}</h3>
                <p class="detail-body">${esc(listing.description)}</p>
                <div class="row" style="margin-top:16px">${favoriteButton}</div>
                ${ownerActions}
            </div>

            <div class="stack">
                <div class="card tight">
                    <h3>${t('detail.itemDetails')}</h3>
                    <dl class="kv">
                        <dt>${t('detail.price')}</dt><dd>${money(listing.price)}</dd>
                        <dt>${t('detail.condition')}</dt><dd>${esc(conditionLabel(listing.condition))}</dd>
                        <dt>${t('detail.category')}</dt><dd>${esc(categoryLabel(listing.category))}</dd>
                        <dt>${t('detail.status')}</dt><dd>${statusLabel(listing.status)}</dd>
                        <dt>${t('detail.seller')}</dt><dd>${esc(listing.sellerName)}</dd>
                        <dt>${t('detail.favoritedBy')}</dt><dd>${listing.favoriteCount}</dd>
                        <dt>${t('detail.lastUpdate')}</dt><dd>${dateStr(listing.updatedAt)}</dd>
                    </dl>
                </div>
                <div class="card tight">
                    <h3>${t('detail.statusHistory')}</h3>
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
                    toast(t('toast.favRemoved'));
                } else {
                    await api('/favorites/' + listing.id, { method: 'POST' });
                    toast(t('toast.favAdded'));
                }
                listingView(id);
            } catch (error) {
                toast(error.message, true);
            }
        });
    }

    const act = (elementId, target, messageKey) => {
        const button = document.getElementById(elementId);
        if (!button) {
            return;
        }
        button.addEventListener('click', async () => {
            try {
                await api('/listings/' + listing.id + '/status', { method: 'PUT', body: { status: target } });
                toast(t(messageKey));
                listingView(id);
                refreshUnread();
            } catch (error) {
                toast(error.message, true);
            }
        });
    };
    act('act-reserve', 'RESERVED', 'toast.reserved');
    act('act-sold', 'SOLD', 'toast.sold');
    act('act-available', 'AVAILABLE', 'toast.available');

    const deleteButton = document.getElementById('act-delete');
    if (deleteButton) {
        deleteButton.addEventListener('click', async () => {
            if (!window.confirm(t('detail.confirmDelete'))) {
                return;
            }
            try {
                await api('/listings/' + listing.id, { method: 'DELETE' });
                toast(t('toast.deleted'));
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
                <h2>${t('publish.notOwner')}</h2><a class="btn" href="#/home">${t('common.backToBrowse')}</a></div>`;
            return;
        }
        existing = detail.listing;
    }

    document.getElementById('view').innerHTML = `
        <div class="page-head">
            <div>
                <h1>${existing ? t('publish.titleEdit') : t('publish.titleNew')}</h1>
                <p>${t('publish.subtitle')}</p>
            </div>
            <a class="btn" href="#/my-listings">${t('publish.myListings')}</a>
        </div>
        <form class="card" id="listing-form" style="max-width:660px">
            <div class="field">
                <label for="l-title">${t('form.title')}</label>
                <input id="l-title" maxlength="100" required placeholder="${t('form.titlePh')}" value="${esc(existing ? existing.title : '')}">
            </div>
            <div class="field-row">
                <div class="field">
                    <label for="l-category">${t('form.category')}</label>
                    <select id="l-category">${categoryOptions(existing ? existing.category : '')}</select>
                </div>
                <div class="field">
                    <label for="l-condition">${t('form.condition')}</label>
                    <select id="l-condition">${conditionOptions(existing ? existing.condition : 'GOOD')}</select>
                </div>
            </div>
            <div class="field">
                <label for="l-price">${t('form.price')}</label>
                <input id="l-price" type="number" min="0.01" step="0.01" required value="${existing ? existing.price : ''}">
            </div>
            <div class="field">
                <label for="l-description">${t('form.description')}</label>
                <textarea id="l-description" maxlength="2000" required placeholder="${t('form.descriptionPh')}">${esc(existing ? existing.description : '')}</textarea>
            </div>
            <div class="field">
                <label for="l-photo">${t('form.photo')}</label>
                <input id="l-photo" maxlength="300" placeholder="https://…" value="${esc(existing && existing.photoUrl ? existing.photoUrl : '')}">
                <span class="hint">${t('publish.photoHint')}</span>
            </div>
            <div class="row">
                <button class="btn btn-primary" type="submit">${existing ? t('publish.submitEdit') : t('publish.submitNew')}</button>
                <button class="btn" type="button" id="cancel-btn">${t('common.cancel')}</button>
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
            toast(t(existing ? 'toast.updated' : 'toast.published'));
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
                <div class="listing-meta">${esc(categoryLabel(listing.category))} · ${esc(conditionLabel(listing.condition))}</div></td>
            <td>${money(listing.price)}</td>
            <td><span class="badge-pill status-${listing.status}">${statusLabel(listing.status)}</span></td>
            <td>${listing.favoriteCount}</td>
            <td>${dayStr(listing.createdAt)}</td>
            <td class="row" style="gap:6px">
                ${listing.status !== 'SOLD' ? `<a class="btn btn-sm" href="#/edit/${listing.id}">${t('common.edit')}</a>` : ''}
                ${listing.status === 'AVAILABLE' ? `<button class="btn btn-sm" data-reserve="${listing.id}">${t('my.reserve')}</button>` : ''}
                ${listing.status === 'RESERVED' ? `<button class="btn btn-sm" data-sold="${listing.id}">${t('my.sold')}</button>` : ''}
                <button class="btn btn-sm btn-danger" data-delete="${listing.id}">${t('common.delete')}</button>
            </td>
        </tr>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>${t('my.title')}</h1>
                <p>${t(listings.length === 1 ? 'my.subtitleOne' : 'my.subtitleMany', { n: listings.length })}</p></div>
            <a class="btn btn-primary" href="#/publish">${t('my.new')}</a>
        </div>
        ${listings.length ? `
        <div class="card" style="padding:6px 14px">
            <table>
                <thead><tr>
                    <th>${t('my.item')}</th><th>${t('my.price')}</th><th>${t('my.status')}</th>
                    <th>${t('my.favorites')}</th><th>${t('my.published')}</th><th></th>
                </tr></thead>
                <tbody>${rows}</tbody>
            </table>
        </div>` : `<div class="empty"><span class="big">📦</span><h2>${t('my.emptyTitle')}</h2>
            <p>${t('my.emptyHint')}</p><a class="btn btn-primary" href="#/publish">${t('my.sell')}</a></div>`}`;

    const change = async (id, status, messageKey) => {
        try {
            await api('/listings/' + id + '/status', { method: 'PUT', body: { status } });
            toast(t(messageKey));
            myListingsView();
        } catch (error) {
            toast(error.message, true);
        }
    };
    view.querySelectorAll('[data-reserve]').forEach((button) => {
        button.addEventListener('click', () => change(button.dataset.reserve, 'RESERVED', 'toast.reserved'));
    });
    view.querySelectorAll('[data-sold]').forEach((button) => {
        button.addEventListener('click', () => change(button.dataset.sold, 'SOLD', 'toast.sold'));
    });
    view.querySelectorAll('[data-delete]').forEach((button) => {
        button.addEventListener('click', async () => {
            if (!window.confirm(t('detail.confirmDelete'))) {
                return;
            }
            try {
                await api('/listings/' + button.dataset.delete, { method: 'DELETE' });
                toast(t('toast.deleted'));
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
                        <span class="badge-pill ${request.status === 'ACTIVE' ? 'status-AVAILABLE' : 'status-SOLD'}">${t('reqState.' + request.status)}</span>
                    </div>
                    <div class="listing-meta" style="margin-top:4px">
                        ${esc(categoryLabel(request.category))} ·
                        ${t('req.budget', { min: money(request.minBudget), max: money(request.maxBudget) })} ·
                        ${t('req.acceptableCondition', { condition: conditionLabel(request.acceptableCondition) })}
                    </div>
                    ${request.requirements ? `<div class="listing-meta" style="margin-top:4px">${esc(request.requirements)}</div>` : ''}
                </div>
                ${request.status === 'ACTIVE' ? `<button class="btn btn-sm btn-danger" data-close="${request.id}">${t('req.close')}</button>` : ''}
            </div>
        </div>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>${t('req.title')}</h1><p>${t('req.subtitle')}</p></div>
        </div>
        <div class="grid split">
            <div class="stack">
                <h2>${t('req.mine', { n: requests.length })}</h2>
                ${requests.length ? list : `<div class="empty"><span class="big">📝</span><p>${t('req.emptyHint')}</p></div>`}
            </div>
            <form class="card" id="request-form">
                <h3>${t('req.newTitle')}</h3>
                <div class="field">
                    <label for="r-keywords">${t('req.keywords')}</label>
                    <input id="r-keywords" maxlength="200" required placeholder="${t('req.keywordsPh')}">
                </div>
                <div class="field">
                    <label for="r-category">${t('form.category')}</label>
                    <select id="r-category">${categoryOptions('')}</select>
                </div>
                <div class="field-row">
                    <div class="field">
                        <label for="r-min">${t('req.minBudget')}</label>
                        <input id="r-min" type="number" min="0.01" step="0.01" required>
                    </div>
                    <div class="field">
                        <label for="r-max">${t('req.maxBudget')}</label>
                        <input id="r-max" type="number" min="0.01" step="0.01" required>
                    </div>
                </div>
                <div class="field">
                    <label for="r-condition">${t('req.condition')}</label>
                    <select id="r-condition">${conditionOptions('GOOD')}</select>
                </div>
                <div class="field">
                    <label for="r-requirements">${t('req.requirements')}</label>
                    <textarea id="r-requirements" maxlength="500" style="min-height:70px"></textarea>
                </div>
                <button class="btn btn-primary btn-block" type="submit">${t('req.publish')}</button>
            </form>
        </div>`;

    view.querySelectorAll('[data-close]').forEach((button) => {
        button.addEventListener('click', async () => {
            try {
                await api('/requests/' + button.dataset.close + '/close', { method: 'POST' });
                toast(t('toast.reqClosed'));
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
            toast(t('toast.reqPublished'));
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
                            <span class="badge-pill badge-accent">${t(match.myRole === 'SELLER' ? 'match.itemFound' : 'match.requestFound')}</span>
                            <span class="badge-pill badge-soft">${t('match.matchedOn', { date: dayStr(match.createdAt) })}</span>
                        </div>
                        <h3 style="margin:8px 0 2px">
                            <a href="#/listing/${listing.id}">${esc(listing.title)}</a>
                            <span style="font-weight:600;color:var(--text-soft);font-size:14px">· ${money(listing.price)}</span>
                        </h3>
                        <div class="listing-meta">
                            ${t('match.listingLine', {
                                condition: conditionLabel(listing.condition),
                                category: categoryLabel(listing.category),
                                seller: listing.sellerName
                            })}
                        </div>
                        <div class="listing-meta" style="margin-top:6px">
                            ${t('match.requestLine', {
                                keywords: request.keywords,
                                min: money(request.minBudget),
                                max: money(request.maxBudget),
                                buyer: request.buyerName
                            })}
                        </div>
                        <div class="score-bars">
                            ${bar(t('match.ruleCategory'), match.categoryScore)}
                            ${bar(t('match.rulePrice'), match.priceScore)}
                            ${bar(t('match.ruleCondition'), match.conditionScore)}
                            ${bar(t('match.ruleKeyword'), match.keywordScore)}
                        </div>
                    </div>
                </div>
            </div>`;
    }).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>${t('match.title')}</h1><p>${t('match.subtitle')}</p></div>
            <a class="btn" href="#/requests">${t('match.myRequests')}</a>
        </div>
        ${matches.length ? `<div class="stack">${cards}</div>` : `
            <div class="empty">
                <span class="big">🤝</span>
                <h2>${t('match.emptyTitle')}</h2>
                <p>${t('match.emptyHint')}</p>
                <a class="btn btn-primary" href="#/publish">${t('match.sell')}</a>
                <a class="btn" href="#/requests">${t('match.postRequest')}</a>
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
            <div><h1>${t('fav.title')}</h1><p>${t('fav.subtitle')}</p></div>
        </div>
        ${favorites.length
            ? `<div class="grid cards">${favorites.map(listingCard).join('')}</div>`
            : `<div class="empty"><span class="big">☆</span><h2>${t('fav.emptyTitle')}</h2>
               <p>${t('fav.emptyHint')}</p>
               <a class="btn btn-primary" href="#/home">${t('fav.browse')}</a></div>`}`;
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
                        ${item.read ? '' : `<span class="badge-pill badge-accent">${t('common.new')}</span>`}
                    </div>
                    <div style="color:var(--text-soft);font-size:13.5px;margin-top:4px">${esc(item.body)}</div>
                    <div class="listing-meta" style="margin-top:6px">
                        ${dateStr(item.createdAt)}
                        ${item.listingId ? `· <a href="#/listing/${item.listingId}">${t('notif.openListing')}</a>` : ''}
                    </div>
                </div>
                ${item.read ? '' : `<button class="btn btn-sm btn-ghost" data-read="${item.id}">${t('common.markRead')}</button>`}
            </div>
        </div>`).join('');

    view.innerHTML = `
        <div class="page-head">
            <div><h1>${t('notif.title')}</h1><p>${t('notif.subtitle')}</p></div>
            <button class="btn" id="read-all">${t('notif.markAll')}</button>
        </div>
        ${page.items.length ? `<div class="stack">${items}</div>` : `<div class="empty"><span class="big">🔔</span>
            <h2>${t('notif.emptyTitle')}</h2><p>${t('notif.emptyHint')}</p></div>`}`;

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
        <div class="page-head"><div><h1>${t('profile.title')}</h1>
            <p>${t('profile.subtitle')}</p></div></div>
        <div class="grid split">
            <div class="stack">
                <form class="card" id="profile-form">
                    <h3>${t('profile.account')}</h3>
                    <div class="field">
                        <label for="p-name">${t('profile.name')}</label>
                        <input id="p-name" value="${esc(user.name)}" maxlength="60" required>
                    </div>
                    <div class="field">
                        <label for="p-contact">${t('profile.contact')}</label>
                        <input id="p-contact" value="${esc(user.contact || '')}" maxlength="120">
                    </div>
                    <dl class="kv" style="margin:6px 0 16px">
                        <dt>${t('profile.studentNo')}</dt><dd>${esc(user.studentNo)}</dd>
                        <dt>${t('profile.email')}</dt><dd>${esc(user.email)}</dd>
                        <dt>${t('profile.memberSince')}</dt><dd>${dayStr(user.createdAt)}</dd>
                    </dl>
                    <button class="btn btn-primary" type="submit">${t('profile.save')}</button>
                </form>

                <form class="card" id="password-form">
                    <h3>${t('profile.changePassword')}</h3>
                    <div class="field">
                        <label for="p-old">${t('profile.current')}</label>
                        <input id="p-old" type="password" required>
                    </div>
                    <div class="field">
                        <label for="p-new">${t('profile.newPwd')}</label>
                        <input id="p-new" type="password" minlength="8" required>
                    </div>
                    <button class="btn" type="submit">${t('profile.updatePwd')}</button>
                </form>
            </div>
            <div class="card tight">
                <h3>${t('profile.deleteTitle')}</h3>
                <p style="font-size:13.5px;color:var(--text-soft)">${t('profile.deleteText')}</p>
                <button class="btn btn-danger btn-block" id="delete-account">${t('profile.deleteButton')}</button>
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
            toast(t('toast.profileUpdated'));
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
            toast(t('toast.pwdChanged'));
            event.target.reset();
        } catch (error) {
            toast(error.message, true);
        }
    });

    document.getElementById('delete-account').addEventListener('click', async () => {
        if (!window.confirm(t('profile.confirmDelete'))) {
            return;
        }
        try {
            await api('/me', { method: 'DELETE' });
            state.user = null;
            state.unread = 0;
            renderChrome();
            toast(t('toast.accountDeleted'));
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
                <h2>${t('login.title')}</h2>
                <p style="color:var(--text-soft);font-size:13.5px">${t('login.subtitle')}</p>
                <form id="login-form">
                    <div class="field">
                        <label for="login-id">${t('login.identifier')}</label>
                        <input id="login-id" required autocomplete="username">
                    </div>
                    <div class="field">
                        <label for="login-password">${t('login.password')}</label>
                        <input id="login-password" type="password" required autocomplete="current-password">
                    </div>
                    <button class="btn btn-primary btn-block" type="submit">${t('login.submit')}</button>
                </form>
                <p style="margin-top:14px;font-size:13.5px">${t('login.noAccount')} <a href="#/register">${t('common.register')}</a></p>
                <div class="demo-hint">
                    <strong>${t('login.demoTitle')}</strong><br>
                    ${t('login.demoSeller')} <code>1240016186</code> · password <code>Password123</code><br>
                    ${t('login.demoBuyer')} <code>1240008865</code>
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
            toast(t('toast.welcomeBack', { name: getLang() === 'zh' ? state.user.name : state.user.name.split(' ')[0] }));
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
                <h2>${t('register.title')}</h2>
                <p style="color:var(--text-soft);font-size:13.5px">${t('register.subtitle')}</p>
                <form id="register-form">
                    <div class="field">
                        <label for="reg-name">${t('reg.name')}</label>
                        <input id="reg-name" maxlength="60" required>
                    </div>
                    <div class="field">
                        <label for="reg-no">${t('reg.studentNo')}</label>
                        <input id="reg-no" inputmode="numeric" pattern="[0-9]{6,20}" required>
                    </div>
                    <div class="field">
                        <label for="reg-email">${t('reg.email')}</label>
                        <input id="reg-email" type="email" maxlength="120" required>
                    </div>
                    <div class="field-row">
                        <div class="field">
                            <label for="reg-password">${t('reg.password')}</label>
                            <input id="reg-password" type="password" minlength="8" required>
                        </div>
                        <div class="field">
                            <label for="reg-confirm">${t('reg.confirm')}</label>
                            <input id="reg-confirm" type="password" minlength="8" required>
                        </div>
                    </div>
                    <button class="btn btn-primary btn-block" type="submit">${t('reg.submit')}</button>
                </form>
                <p style="margin-top:14px;font-size:13.5px">${t('register.haveAccount')} <a href="#/login">${t('common.login')}</a></p>
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
            toast(t('toast.welcome'));
            go('#/home');
        } catch (error) {
            toast(error.message, true);
        }
    });
}

/* -------------------------------------------------------------------- boot */

function bindLanguageSwitch() {
    document.getElementById('lang-switch').addEventListener('click', async (event) => {
        const button = event.target.closest('[data-lang]');
        if (!button || button.dataset.lang === getLang()) {
            return;
        }
        setLang(button.dataset.lang);
        applyStaticTranslations();
        renderChrome();
        await render();
    });
}

async function boot() {
    initLang();
    setLang(getLang());
    applyStaticTranslations();
    bindLanguageSwitch();

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

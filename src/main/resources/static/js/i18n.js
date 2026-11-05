/* Bilingual interface (English / 中文).
   Every user-facing string lives here, so switching the language never needs a page reload. */

const I18N = {
    en: {
        'app.title': 'Campus Loop · MUST Second-hand Marketplace',
        'app.tagline': 'MUST second-hand marketplace',
        'footer.team': 'Group 11 · SeekDeep · Software Engineering, MUST',
        'footer.rule': 'Rule-based matching: 0.40 category + 0.35 price + 0.25 condition, shown above 0.70',

        'nav.browse': 'Browse',
        'nav.matches': 'Matches',
        'nav.favorites': 'Favorites',
        'nav.myListings': 'My listings',
        'nav.requests': 'Requests',
        'nav.sell': '+ Sell an item',
        'nav.notifications': 'Notifications',

        'menu.profile': 'Profile & settings',
        'menu.myListings': 'My listings',
        'menu.myRequests': 'My requests',
        'menu.logout': 'Log out',

        'common.login': 'Log in',
        'common.register': 'Register',
        'common.loading': 'Loading…',
        'common.previous': 'Previous',
        'common.next': 'Next',
        'common.pageOf': 'Page {page} of {total}',
        'common.cancel': 'Cancel',
        'common.edit': 'Edit',
        'common.delete': 'Delete',
        'common.markRead': 'Mark read',
        'common.new': 'New',
        'common.backToBrowse': '← Back to browse',
        'common.pageNotFound': 'Page not found',
        'common.somethingWrong': 'Something went wrong',
        'common.pleaseLogin': 'Please log in',
        'common.loginRequired': 'This page belongs to your account.',
        'common.createAccount': 'Create an account',
        'common.saved': 'Save',
        'common.saveState': '★ Saved',
        'common.unsaved': '☆ Save',
        'common.students': 'students',
        'common.student': 'student',

        'home.title': 'Buy and sell inside MUST',
        'home.subtitle': 'Textbooks, electronics, furniture and more — post a request and let the platform find the match.',
        'home.postRequest': 'Post a want-to-buy request',
        'home.countOne': '{n} listing available',
        'home.countMany': '{n} listings available',
        'home.emptyTitle': 'No listing matches these filters',
        'home.emptyHint': 'Try a shorter keyword, a wider price range or fewer conditions.',
        'home.clearFilters': 'Clear all filters',
        'home.searching': 'Searching…',

        'filter.keyword': 'Keyword',
        'filter.keywordPh': 'Search title or description',
        'filter.category': 'Category',
        'filter.allCategories': 'All categories',
        'filter.minPrice': 'Min price',
        'filter.maxPrice': 'Max price',
        'filter.anyPrice': 'Any',
        'filter.zero': '0',
        'filter.sort': 'Sort by',
        'filter.condition': 'Condition',
        'filter.reset': 'Reset',
        'filter.search': 'Search',
        'sort.newest': 'Newest first',
        'sort.priceAsc': 'Price: low to high',
        'sort.priceDesc': 'Price: high to low',

        'detail.description': 'Description',
        'detail.sellerActions': 'Seller actions',
        'detail.reserve': 'Mark as reserved',
        'detail.markSold': 'Mark as sold',
        'detail.markAvailable': 'Back to available',
        'detail.statusHint': 'Available → Reserved → Sold. Sold listings cannot be edited or restored in version 1.',
        'detail.itemDetails': 'Item details',
        'detail.price': 'Price',
        'detail.condition': 'Condition',
        'detail.category': 'Category',
        'detail.status': 'Status',
        'detail.seller': 'Seller',
        'detail.favoritedBy': 'Favorited by',
        'detail.lastUpdate': 'Last update',
        'detail.statusHistory': 'Status history',
        'detail.noHistory': 'No status change yet.',
        'detail.createdAs': 'Created as {status}',
        'detail.listedOn': 'Listed {date} · {seller}',
        'detail.confirmDelete': 'Delete this listing together with its favorites and matches?',

        'publish.titleNew': 'Sell an item',
        'publish.titleEdit': 'Edit listing',
        'publish.subtitle': 'Five fields, three clicks: title, category, condition and price are enough to publish.',
        'publish.myListings': 'My listings',
        'publish.notOwner': 'You cannot edit this listing',
        'publish.photoHint': 'Version 1 accepts one photo per listing.',
        'publish.submitNew': 'Publish listing',
        'publish.submitEdit': 'Save changes',

        'form.title': 'Title',
        'form.titlePh': 'e.g. 24-inch IPS monitor 75Hz HDMI',
        'form.category': 'Category',
        'form.condition': 'Condition',
        'form.price': 'Asking price (MOP)',
        'form.description': 'Description',
        'form.descriptionPh': 'Age, condition details, what is included, pick-up place…',
        'form.photo': 'Photo URL (optional)',

        'my.title': 'My listings',
        'my.subtitleOne': '{n} listing in total. Active listings per user are limited to 50.',
        'my.subtitleMany': '{n} listings in total. Active listings per user are limited to 50.',
        'my.new': '+ New listing',
        'my.item': 'Item',
        'my.price': 'Price',
        'my.status': 'Status',
        'my.favorites': 'Favorites',
        'my.published': 'Published',
        'my.reserve': 'Reserve',
        'my.sold': 'Sold',
        'my.emptyTitle': 'No listings yet',
        'my.emptyHint': 'Publish your first item in under a minute.',
        'my.sell': 'Sell an item',

        'req.title': 'Want-to-buy requests',
        'req.subtitle': 'Post what you need; the matcher compares it with every active listing within seconds.',
        'req.mine': 'My requests ({n}/20)',
        'req.newTitle': 'New request',
        'req.keywords': 'What are you looking for?',
        'req.keywordsPh': 'e.g. monitor 24 inch hdmi ips',
        'req.minBudget': 'Min budget',
        'req.maxBudget': 'Max budget',
        'req.condition': 'Acceptable condition',
        'req.requirements': 'Other requirements (optional)',
        'req.publish': 'Publish request',
        'req.close': 'Close request',
        'req.active': 'Active',
        'req.closed': 'Closed',
        'req.budget': 'Budget {min} – {max}',
        'req.acceptableCondition': 'Acceptable condition: {condition}',
        'req.emptyTitle': 'No request yet',
        'req.emptyHint': 'Describe the item you are looking for.',

        'match.title': 'Matches',
        'match.subtitle': 'Open matches only, ranked by score. Score = 0.40 × category + 0.35 × price + 0.25 × condition; pairs above 0.70 are shown.',
        'match.myRequests': 'My requests',
        'match.itemFound': 'Your item · buyer found',
        'match.requestFound': 'Your request · item found',
        'match.matchedOn': 'Matched {date}',
        'match.requestLine': 'Request: “{keywords}” · budget {min} – {max} · buyer {buyer}',
        'match.listingLine': '{condition} · {category} · seller {seller}',
        'match.ruleCategory': 'Category 0.40',
        'match.rulePrice': 'Price 0.35',
        'match.ruleCondition': 'Condition 0.25',
        'match.ruleKeyword': 'Keyword (gate)',
        'match.emptyTitle': 'No open match yet',
        'match.emptyHint': 'Publish a listing or a want-to-buy request. The matcher runs automatically after every change.',
        'match.sell': 'Sell an item',
        'match.postRequest': 'Post a request',

        'fav.title': 'Favorites',
        'fav.subtitle': 'You are notified when a saved item changes price or status.',
        'fav.emptyTitle': 'No saved item',
        'fav.emptyHint': 'Use the Save button on any listing to keep it here.',
        'fav.browse': 'Browse listings',

        'notif.title': 'Notifications',
        'notif.subtitle': 'Matches, status changes and price changes on saved items. Kept for 90 days.',
        'notif.markAll': 'Mark all as read',
        'notif.openListing': 'Open listing',
        'notif.emptyTitle': 'Nothing yet',
        'notif.emptyHint': 'Notifications appear here as soon as a match is found.',

        'profile.title': 'Profile & settings',
        'profile.subtitle': 'Personal data is visible only to you; other students see your display name on listings.',
        'profile.account': 'Account',
        'profile.name': 'Display name',
        'profile.contact': 'Contact (phone, WeChat…)',
        'profile.studentNo': 'Student number',
        'profile.email': 'E-mail',
        'profile.memberSince': 'Member since',
        'profile.save': 'Save profile',
        'profile.changePassword': 'Change password',
        'profile.current': 'Current password',
        'profile.newPwd': 'New password (at least 8 characters)',
        'profile.updatePwd': 'Update password',
        'profile.deleteTitle': 'Delete account',
        'profile.deleteText': 'Deleting the account also removes your listings, requests, favorites, matches and notifications. This cannot be undone.',
        'profile.deleteButton': 'Delete my account',
        'profile.confirmDelete': 'Delete your account and all related data? This cannot be undone.',

        'login.title': 'Log in',
        'login.subtitle': 'Use your e-mail or your student number.',
        'login.identifier': 'E-mail or student number',
        'login.password': 'Password',
        'login.submit': 'Log in',
        'login.noAccount': 'No account yet?',
        'login.demoTitle': 'Demo account',
        'login.demoSeller': 'Seller demo: student number',
        'login.demoBuyer': 'Buyer demo:',

        'register.title': 'Create an account',
        'register.subtitle': 'MUST students only. Five fields, less than two minutes.',
        'reg.name': 'Name',
        'reg.studentNo': 'Student number',
        'reg.email': 'E-mail',
        'reg.password': 'Password',
        'reg.confirm': 'Repeat password',
        'reg.submit': 'Register',
        'register.haveAccount': 'Already registered?',

        'toast.loggedOut': 'Logged out',
        'toast.welcomeBack': 'Welcome back, {name}',
        'toast.welcome': 'Welcome to Campus Loop',
        'toast.favAdded': 'Saved to favorites',
        'toast.favRemoved': 'Removed from favorites',
        'toast.reserved': 'Listing reserved, matched buyers were notified',
        'toast.sold': 'Listing marked as sold',
        'toast.available': 'Listing is available again',
        'toast.deleted': 'Listing deleted',
        'toast.published': 'Listing published, matching started',
        'toast.updated': 'Listing updated',
        'toast.reqPublished': 'Request published, matching started',
        'toast.reqClosed': 'Request closed',
        'toast.profileUpdated': 'Profile updated',
        'toast.pwdChanged': 'Password changed',
        'toast.accountDeleted': 'Account deleted',

        'state.AVAILABLE': 'Available',
        'state.RESERVED': 'Reserved',
        'state.SOLD': 'Sold',
        'reqState.ACTIVE': 'Active',
        'reqState.CLOSED': 'Closed',

        'cat.BOOKS': 'Books & Notes',
        'cat.ELECTRONICS': 'Electronics',
        'cat.FURNITURE': 'Furniture',
        'cat.DAILY_SUPPLIES': 'Daily Supplies',
        'cat.CLOTHING': 'Clothing',
        'cat.SPORTS': 'Sports & Fitness',
        'cat.OTHER': 'Other',

        'cond.NEW': 'New',
        'cond.LIKE_NEW': 'Like new',
        'cond.GOOD': 'Good',
        'cond.FAIR': 'Fair'
    },

    zh: {
        'app.title': 'Campus Loop · 澳门科技大学二手交易平台',
        'app.tagline': '澳门科技大学校园二手交易',
        'footer.team': '第 11 组 · SeekDeep · 软件工程课程，澳门科技大学',
        'footer.rule': '规则化撮合：0.40 × 类别 + 0.35 × 价格 + 0.25 × 成色，得分高于 0.70 才展示',

        'nav.browse': '逛市场',
        'nav.matches': '我的匹配',
        'nav.favorites': '我的收藏',
        'nav.myListings': '我的商品',
        'nav.requests': '求购',
        'nav.sell': '+ 发布商品',
        'nav.notifications': '通知',

        'menu.profile': '个人资料与设置',
        'menu.myListings': '我的商品',
        'menu.myRequests': '我的求购',
        'menu.logout': '退出登录',

        'common.login': '登录',
        'common.register': '注册',
        'common.loading': '加载中…',
        'common.previous': '上一页',
        'common.next': '下一页',
        'common.pageOf': '第 {page} / {total} 页',
        'common.cancel': '取消',
        'common.edit': '编辑',
        'common.delete': '删除',
        'common.markRead': '标为已读',
        'common.new': '新',
        'common.backToBrowse': '← 返回列表',
        'common.pageNotFound': '页面不存在',
        'common.somethingWrong': '出了点问题',
        'common.pleaseLogin': '请先登录',
        'common.loginRequired': '这个页面属于你的账号。',
        'common.createAccount': '注册新账号',
        'common.saved': '收藏',
        'common.saveState': '★ 已收藏',
        'common.unsaved': '☆ 收藏',
        'common.students': '位同学',
        'common.student': '位同学',

        'home.title': '在校园里买和卖',
        'home.subtitle': '教材、电子产品、家具……发布一条求购，让系统自动帮你找人。',
        'home.postRequest': '发布求购需求',
        'home.countOne': '共 {n} 件在售商品',
        'home.countMany': '共 {n} 件在售商品',
        'home.emptyTitle': '没有符合条件的商品',
        'home.emptyHint': '试试更短的关键词、更宽的价格区间，或少选几个成色。',
        'home.clearFilters': '清空全部筛选',
        'home.searching': '搜索中…',

        'filter.keyword': '关键词',
        'filter.keywordPh': '搜索标题或描述',
        'filter.category': '类别',
        'filter.allCategories': '全部类别',
        'filter.minPrice': '最低价',
        'filter.maxPrice': '最高价',
        'filter.anyPrice': '不限',
        'filter.zero': '0',
        'filter.sort': '排序',
        'filter.condition': '成色',
        'filter.reset': '重置',
        'filter.search': '搜索',
        'sort.newest': '最新发布',
        'sort.priceAsc': '价格从低到高',
        'sort.priceDesc': '价格从高到低',

        'detail.description': '商品描述',
        'detail.sellerActions': '卖家操作',
        'detail.reserve': '标记为已预订',
        'detail.markSold': '标记为已售出',
        'detail.markAvailable': '恢复为在售',
        'detail.statusHint': '在售 → 已预订 → 已售出。第一版中已售出的商品不能再编辑或恢复。',
        'detail.itemDetails': '商品信息',
        'detail.price': '价格',
        'detail.condition': '成色',
        'detail.category': '类别',
        'detail.status': '状态',
        'detail.seller': '卖家',
        'detail.favoritedBy': '收藏人数',
        'detail.lastUpdate': '最后更新',
        'detail.statusHistory': '状态记录',
        'detail.noHistory': '暂无状态变更。',
        'detail.createdAs': '创建时为 {status}',
        'detail.listedOn': '{date} 发布 · {seller}',
        'detail.confirmDelete': '确定删除这件商品，以及它的收藏和匹配记录吗？',

        'publish.titleNew': '发布商品',
        'publish.titleEdit': '编辑商品',
        'publish.subtitle': '五个字段、三次点击：标题、类别、成色、价格就能发布。',
        'publish.myListings': '我的商品',
        'publish.notOwner': '你不能编辑这件商品',
        'publish.photoHint': '第一版每件商品支持一张图片。',
        'publish.submitNew': '发布商品',
        'publish.submitEdit': '保存修改',

        'form.title': '标题',
        'form.titlePh': '例如：24 寸 IPS 显示器 75Hz HDMI',
        'form.category': '类别',
        'form.condition': '成色',
        'form.price': '售价（MOP）',
        'form.description': '描述',
        'form.descriptionPh': '使用了多久、有什么瑕疵、包含哪些配件、在哪里取…',
        'form.photo': '图片链接（选填）',

        'my.title': '我的商品',
        'my.subtitleOne': '共 {n} 件商品。每人最多同时挂 50 件在售。',
        'my.subtitleMany': '共 {n} 件商品。每人最多同时挂 50 件在售。',
        'my.new': '+ 发布商品',
        'my.item': '商品',
        'my.price': '价格',
        'my.status': '状态',
        'my.favorites': '收藏',
        'my.published': '发布时间',
        'my.reserve': '预订',
        'my.sold': '售出',
        'my.emptyTitle': '还没有发布商品',
        'my.emptyHint': '一分钟就能挂出第一件东西。',
        'my.sell': '发布商品',

        'req.title': '求购需求',
        'req.subtitle': '把你要的东西发出来，撮合引擎会在几秒内和所有在售商品比对。',
        'req.mine': '我的求购（{n}/20）',
        'req.newTitle': '发布求购',
        'req.keywords': '你想找什么？',
        'req.keywordsPh': '例如：显示器 24 寸 hdmi ips',
        'req.minBudget': '预算下限',
        'req.maxBudget': '预算上限',
        'req.condition': '可接受成色',
        'req.requirements': '其他要求（选填）',
        'req.publish': '发布求购',
        'req.close': '关闭求购',
        'req.active': '进行中',
        'req.closed': '已关闭',
        'req.budget': '预算 {min} – {max}',
        'req.acceptableCondition': '可接受成色：{condition}',
        'req.emptyTitle': '还没有求购',
        'req.emptyHint': '写下你要找的东西。',

        'match.title': '匹配结果',
        'match.subtitle': '只显示进行中的匹配，按得分排序。得分 = 0.40 × 类别 + 0.35 × 价格 + 0.25 × 成色，高于 0.70 才展示。',
        'match.myRequests': '我的求购',
        'match.itemFound': '我的商品 · 有人想要',
        'match.requestFound': '我的求购 · 有货了',
        'match.matchedOn': '{date} 匹配',
        'match.requestLine': '求购：“{keywords}” · 预算 {min} – {max} · 买家 {buyer}',
        'match.listingLine': '{condition} · {category} · 卖家 {seller}',
        'match.ruleCategory': '类别 0.40',
        'match.rulePrice': '价格 0.35',
        'match.ruleCondition': '成色 0.25',
        'match.ruleKeyword': '关键词（门槛）',
        'match.emptyTitle': '暂时没有匹配',
        'match.emptyHint': '发布商品或求购，每次变更后系统都会自动重新撮合。',
        'match.sell': '发布商品',
        'match.postRequest': '发布求购',

        'fav.title': '我的收藏',
        'fav.subtitle': '收藏的商品改价或改状态时，你会收到通知。',
        'fav.emptyTitle': '还没有收藏',
        'fav.emptyHint': '在任意商品页点“收藏”，就会出现在这里。',
        'fav.browse': '去逛市场',

        'notif.title': '通知',
        'notif.subtitle': '匹配、状态变更、收藏商品改价都会在这里。通知保留 90 天。',
        'notif.markAll': '全部标为已读',
        'notif.openListing': '查看商品',
        'notif.emptyTitle': '暂时没有通知',
        'notif.emptyHint': '一旦撮合到合适的对象，这里就会亮起来。',

        'profile.title': '个人资料与设置',
        'profile.subtitle': '个人信息只有你自己能看到，其他同学只看到你的昵称。',
        'profile.account': '账号信息',
        'profile.name': '显示昵称',
        'profile.contact': '联系方式（手机、微信…）',
        'profile.studentNo': '学号',
        'profile.email': '邮箱',
        'profile.memberSince': '注册时间',
        'profile.save': '保存资料',
        'profile.changePassword': '修改密码',
        'profile.current': '当前密码',
        'profile.newPwd': '新密码（至少 8 位）',
        'profile.updatePwd': '更新密码',
        'profile.deleteTitle': '注销账号',
        'profile.deleteText': '注销会同时删除你的商品、求购、收藏、匹配和通知，且无法恢复。',
        'profile.deleteButton': '注销我的账号',
        'profile.confirmDelete': '确定注销账号并删除所有相关数据？此操作无法恢复。',

        'login.title': '登录',
        'login.subtitle': '用邮箱或学号登录。',
        'login.identifier': '邮箱或学号',
        'login.password': '密码',
        'login.submit': '登录',
        'login.noAccount': '还没有账号？',
        'login.demoTitle': '演示账号',
        'login.demoSeller': '卖家演示：学号',
        'login.demoBuyer': '买家演示：',

        'register.title': '注册账号',
        'register.subtitle': '仅限澳门科技大学学生。五个字段，两分钟内完成。',
        'reg.name': '姓名',
        'reg.studentNo': '学号',
        'reg.email': '邮箱',
        'reg.password': '密码',
        'reg.confirm': '再次输入密码',
        'reg.submit': '注册',
        'register.haveAccount': '已经注册过？',

        'toast.loggedOut': '已退出登录',
        'toast.welcomeBack': '欢迎回来，{name}',
        'toast.welcome': '欢迎使用 Campus Loop',
        'toast.favAdded': '已加入收藏',
        'toast.favRemoved': '已取消收藏',
        'toast.reserved': '已标记为预订，匹配到的买家已收到通知',
        'toast.sold': '已标记为售出',
        'toast.available': '已恢复为在售',
        'toast.deleted': '商品已删除',
        'toast.published': '商品已发布，开始撮合',
        'toast.updated': '商品已更新',
        'toast.reqPublished': '求购已发布，开始撮合',
        'toast.reqClosed': '求购已关闭',
        'toast.profileUpdated': '资料已更新',
        'toast.pwdChanged': '密码已修改',
        'toast.accountDeleted': '账号已注销',

        'state.AVAILABLE': '在售',
        'state.RESERVED': '已预订',
        'state.SOLD': '已售出',
        'reqState.ACTIVE': '进行中',
        'reqState.CLOSED': '已关闭',

        'cat.BOOKS': '教材书籍',
        'cat.ELECTRONICS': '电子产品',
        'cat.FURNITURE': '家具',
        'cat.DAILY_SUPPLIES': '生活用品',
        'cat.CLOTHING': '服饰',
        'cat.SPORTS': '运动健身',
        'cat.OTHER': '其他',

        'cond.NEW': '全新',
        'cond.LIKE_NEW': '几乎全新',
        'cond.GOOD': '成色良好',
        'cond.FAIR': '有使用痕迹'
    }
};

/* Backend messages are sent in English; translate the known ones, keep the original otherwise. */
const MESSAGE_EXACT_ZH = {
    'The two passwords do not match': '两次输入的密码不一致',
    'This student number is already registered': '该学号已注册',
    'This e-mail address is already registered': '该邮箱已注册',
    'Invalid credential': '邮箱/学号或密码不正确',
    'Session expired': '登录已过期，请重新登录',
    'Please log in to continue': '请先登录',
    'Something went wrong. Please try again.': '服务出了点问题，请稍后重试',
    'Only available listings can be edited': '只有在售商品可以编辑',
    'You can only manage your own listings': '只能管理自己发布的商品',
    'Listing not found': '商品不存在',
    'A sold listing cannot be saved': '已售出的商品不能收藏',
    'A closed request cannot be edited': '已关闭的求购不能编辑',
    'This request is already closed': '这条求购已经关闭',
    'Request not found': '求购不存在',
    'You can only manage your own requests': '只能管理自己发布的求购',
    'Minimum budget cannot be greater than maximum budget': '预算下限不能大于上限',
    'Minimum price cannot be greater than maximum price': '最低价不能大于最高价',
    'Current password is incorrect': '当前密码不正确',
    'The new password must be different from the current one': '新密码不能与当前密码相同',
    'Title is required': '请填写标题',
    'Title must be at most 100 characters': '标题最多 100 个字符',
    'Category is required': '请选择类别',
    'Description is required': '请填写描述',
    'Description must be at most 2000 characters': '描述最多 2000 个字符',
    'Condition is required': '请选择成色',
    'Price is required': '请填写价格',
    'Price must be greater than 0': '价格必须大于 0',
    'Price must have at most 2 decimal places': '价格最多两位小数',
    'Desired item keywords are required': '请填写你想找的东西',
    'Keywords must be at most 200 characters': '关键词最多 200 个字符',
    'Minimum budget is required': '请填写预算下限',
    'Maximum budget is required': '请填写预算上限',
    'Minimum budget must be greater than 0': '预算下限必须大于 0',
    'Maximum budget must be greater than 0': '预算上限必须大于 0',
    'Budget must have at most 2 decimal places': '预算最多两位小数',
    'Acceptable condition is required': '请选择可接受成色',
    'Requirements must be at most 500 characters': '其他要求最多 500 个字符',
    'Name is required': '请填写姓名',
    'Name must be at most 60 characters': '姓名最多 60 个字符',
    'Contact must be at most 120 characters': '联系方式最多 120 个字符',
    'Student number is required': '请填写学号',
    'Student number must be 6-20 digits': '学号必须是 6-20 位数字',
    'E-mail is required': '请填写邮箱',
    'E-mail format is invalid': '邮箱格式不正确',
    'E-mail must be at most 120 characters': '邮箱最多 120 个字符',
    'Password is required': '请填写密码',
    'Password must be at least 8 characters': '密码至少 8 位',
    'Please repeat the password': '请再次输入密码',
    'E-mail or student number is required': '请填写邮箱或学号',
    'Current password is required': '请填写当前密码',
    'New password is required': '请填写新密码',
    'Target status is required': '请选择目标状态'
};

const MESSAGE_RULE_ZH = [
    [/^You already have (\d+) active listings.*$/,
        (m) => `你已有 ${m[1]} 件在售商品，请先标记售出或删除后再发布。`],
    [/^You already have (\d+) active requests.*$/,
        (m) => `你已有 ${m[1]} 条进行中的求购，请先关闭一条。`],
    [/^Account locked after 5 failed attempts\. Try again in (\d+) minute\(s\)\.$/,
        (m) => `连续 5 次登录失败，账号已锁定，请 ${m[1]} 分钟后再试。`],
    [/^You can save at most (\d+) listings$/,
        (m) => `最多只能收藏 ${m[1]} 件商品`],
    [/^Status cannot change from (\w+) to (\w+) in version 1$/,
        (m) => `第一版不支持从「${statusZh(m[1])}」变更为「${statusZh(m[2])}」`],
    [/^The listing is already (\w+)$/,
        (m) => `这件商品已经是「${statusZh(m[1])}」状态`]
];

function statusZh(value) {
    const dict = I18N.zh;
    const key = 'state.' + value;
    return dict[key] || value;
}

let currentLang = 'en';

function initLang() {
    let stored = null;
    try {
        stored = localStorage.getItem('campusloop.lang');
    } catch (ignored) {
        stored = null;
    }
    if (stored === 'zh' || stored === 'en') {
        currentLang = stored;
    } else {
        const browser = (navigator.language || 'en').toLowerCase();
        currentLang = browser.startsWith('zh') ? 'zh' : 'en';
    }
}

function getLang() {
    return currentLang;
}

function setLang(lang) {
    currentLang = lang === 'zh' ? 'zh' : 'en';
    try {
        localStorage.setItem('campusloop.lang', currentLang);
    } catch (ignored) {
        /* private mode: the language still applies for this session */
    }
    document.documentElement.lang = currentLang === 'zh' ? 'zh-CN' : 'en';
}

function t(key, params) {
    const dict = I18N[currentLang] || I18N.en;
    let text = dict[key];
    if (text === undefined) {
        text = I18N.en[key] !== undefined ? I18N.en[key] : key;
    }
    if (params) {
        Object.keys(params).forEach((name) => {
            text = text.split('{' + name + '}').join(params[name]);
        });
    }
    return text;
}

/** Translates a backend message for the current language, falling back to the original text. */
function serverMessage(message) {
    if (currentLang !== 'zh' || !message) {
        return message;
    }
    if (MESSAGE_EXACT_ZH[message]) {
        return MESSAGE_EXACT_ZH[message];
    }
    for (const [pattern, build] of MESSAGE_RULE_ZH) {
        const match = pattern.exec(message);
        if (match) {
            return build(match);
        }
    }
    return message;
}

const localeCode = () => (currentLang === 'zh' ? 'zh-CN' : 'en-GB');

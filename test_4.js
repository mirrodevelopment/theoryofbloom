
/* ═══════════════════════════════════════════════════════════════════════
   HERO BOOK EDITOR ENGINE
═══════════════════════════════════════════════════════════════════════ */
const bookState = { pages: [], currentPage: -1, selectedEl: null };

function initBookEditor() {
    const saved = localStorage.getItem('heroBookPages');
    if (saved) {
        try { bookState.pages = JSON.parse(saved); } catch(e) { bookState.pages = []; }
    }
    renderPageList();
    if (bookState.pages.length > 0) loadPage(0);
}

function renderPageList() {
    const list = document.getElementById('bookPageList');
    if (!list) return;
    list.innerHTML = '';
    bookState.pages.forEach((page, i) => {
        const thumb = document.createElement('div');
        thumb.className = 'book-page-thumb' + (i === bookState.currentPage ? ' active' : '');
        thumb.innerHTML = `<span style="font-size:0.75rem;font-weight:600;color:#555;">Page ${i+1}</span><span style="font-size:0.7rem;color:#aaa;margin-left:6px;">${page.elements ? page.elements.length : 0} elements</span><button class="del-page-btn" onclick="event.stopPropagation();deleteBookPage(${i})"><i class="bi bi-x"></i></button>`;
        thumb.onclick = () => loadPage(i);
        list.appendChild(thumb);
    });
    document.getElementById('bookCanvasPlaceholder').style.display = bookState.pages.length === 0 ? 'block' : 'none';
}

function addBookPage() {
    bookState.pages.push({ elements: [], bg: '#fff', layout: 'blank' });
    renderPageList();
    loadPage(bookState.pages.length - 1);
}

function deleteBookPage(i) {
    if (!confirm('Delete Page ' + (i+1) + '?')) return;
    bookState.pages.splice(i, 1);
    if (bookState.currentPage >= bookState.pages.length) bookState.currentPage = bookState.pages.length - 1;
    renderPageList();
    if (bookState.currentPage >= 0) loadPage(bookState.currentPage);
    else { document.getElementById('bookCanvas').innerHTML = '<div id="bookCanvasPlaceholder" style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);text-align:center;color:#bbb;pointer-events:none;"><i class="bi bi-book" style="font-size:3rem;"></i><br><span style="font-size:0.85rem;">Select a page or add a new one to start editing</span></div>'; }
}

function loadPage(i) {
    saveCurrentPageState();
    bookState.currentPage = i;
    const canvas = document.getElementById('bookCanvas');
    canvas.innerHTML = '';
    const page = bookState.pages[i];
    if (!page) return;
    canvas.style.background = page.bg || '#fff';
    (page.elements || []).forEach(el => {
        const domEl = createDomElement(el);
        canvas.appendChild(domEl);
    });
    renderPageList();
    bookState.selectedEl = null;
    document.getElementById('bookElProps').style.display = 'none';
}

function saveCurrentPageState() {
    if (bookState.currentPage < 0 || !bookState.pages[bookState.currentPage]) return;
    const canvas = document.getElementById('bookCanvas');
    const elements = [];
    canvas.querySelectorAll('.book-el').forEach(el => {
        const obj = { type: el.dataset.type, left: el.style.left, top: el.style.top, zIndex: el.style.zIndex || 1, width: el.style.width, height: el.style.height };
        if (el.dataset.type === 'text') {
            const ta = el.querySelector('textarea');
            obj.text = ta.value;
            obj.fontFamily = ta.style.fontFamily;
            obj.fontSize = ta.style.fontSize;
            obj.color = ta.style.color;
            obj.fontWeight = ta.style.fontWeight;
            obj.fontStyle = ta.style.fontStyle;
            obj.lineHeight = ta.style.lineHeight;
        } else if (el.dataset.type === 'image') {
            obj.src = el.querySelector('img').src;
        }
        elements.push(obj);
    });
    bookState.pages[bookState.currentPage].elements = elements;
}

function createDomElement(el) {
    const wrap = document.createElement('div');
    wrap.className = 'book-el';
    wrap.dataset.type = el.type;
    wrap.style.left = el.left || '50px';
    wrap.style.top = el.top || '50px';
    wrap.style.zIndex = el.zIndex || 1;
    wrap.style.position = 'absolute';
    if (el.width) wrap.style.width = el.width;
    if (el.height) wrap.style.height = el.height;
    
    if (el.type === 'text') {
        wrap.style.resize = 'both';
        wrap.style.overflow = 'hidden';
        const ta = document.createElement('textarea');
        ta.className = 'book-text-el';
        ta.value = el.text || 'Click to edit text';
        ta.style.fontFamily = el.fontFamily || "'Inter', sans-serif";
        ta.style.fontSize = el.fontSize || '18px';
        ta.style.color = el.color || '#2A2621';
        ta.style.fontWeight = el.fontWeight || '400';
        ta.style.fontStyle = el.fontStyle || 'normal';
        ta.style.lineHeight = el.lineHeight || '1.5';
        ta.style.width = '100%';
        ta.style.height = '100%';
        ta.style.border = 'none';
        ta.style.background = 'transparent';
        ta.style.resize = 'none'; // Wrapper handles resize
        ta.style.outline = 'none';
        wrap.appendChild(ta);
    } else if (el.type === 'image') {
        wrap.style.resize = 'both';
        wrap.style.overflow = 'hidden';
        const img = document.createElement('img');
        img.className = 'book-img-el';
        img.src = el.src || '';
        img.alt = 'Book image';
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'cover';
        img.style.pointerEvents = 'none'; // Let wrapper handle drag/resize
        wrap.appendChild(img);
    }
    makeDraggable(wrap);
    wrap.onclick = function(e) { e.stopPropagation(); selectElement(wrap); };
    return wrap;
}

function makeDraggable(el) {
    let startX, startY, origLeft, origTop;
    el.addEventListener('mousedown', function(e) {
        if (e.target.tagName === 'TEXTAREA' || e.target.tagName === 'IMG') {
            if (e.target.tagName === 'TEXTAREA') return;
        }
        e.preventDefault();
        startX = e.clientX; startY = e.clientY;
        origLeft = parseInt(el.style.left) || 0;
        origTop = parseInt(el.style.top) || 0;
        function onMove(e) {
            el.style.left = (origLeft + e.clientX - startX) + 'px';
            el.style.top = (origTop + e.clientY - startY) + 'px';
        }
        function onUp() {
            document.removeEventListener('mousemove', onMove);
            document.removeEventListener('mouseup', onUp);
        }
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
    });
}

function selectElement(el) {
    deselectAll();
    bookState.selectedEl = el;
    el.classList.add('selected');
    const propsPanel = document.getElementById('bookElProps');
    propsPanel.style.display = 'block';
    
    document.getElementById('propWidth').value = parseInt(el.style.width) || '';
    document.getElementById('propHeight').value = parseInt(el.style.height) || '';
    
    if (el.dataset.type === 'text') {
        document.querySelectorAll('.prop-text-only').forEach(p => p.style.display = 'block');
        document.querySelectorAll('.prop-img-only').forEach(p => p.style.display = 'none');
        
        const ta = el.querySelector('textarea');
        document.getElementById('propFontFamily').value = ta.style.fontFamily || "'Inter', sans-serif";
        document.getElementById('propFontSize').value = parseInt(ta.style.fontSize) || 18;
        document.getElementById('propColor').value = rgbToHex(ta.style.color) || '#2A2621';
        document.getElementById('propLineHeight').value = ta.style.lineHeight || 1.5;
        document.getElementById('propBold').checked = ta.style.fontWeight === '700' || ta.style.fontWeight === 'bold';
        document.getElementById('propItalic').checked = ta.style.fontStyle === 'italic';
    } else if (el.dataset.type === 'image') {
        document.querySelectorAll('.prop-text-only').forEach(p => p.style.display = 'none');
        document.querySelectorAll('.prop-img-only').forEach(p => p.style.display = 'block');
    }
}

function deselectAll(e) {
    if (e && e.target.closest('.book-el')) return;
    document.querySelectorAll('.book-el.selected').forEach(el => el.classList.remove('selected'));
    bookState.selectedEl = null;
    document.getElementById('bookElProps').style.display = 'none';
}

function updateSelectedProp(prop, val) {
    if (!bookState.selectedEl) return;
    const isText = bookState.selectedEl.dataset.type === 'text';
    const isImg = bookState.selectedEl.dataset.type === 'image';
    
    if (prop === 'width' || prop === 'height') {
        bookState.selectedEl.style[prop] = val;
    } else if (isText) {
        const ta = bookState.selectedEl.querySelector('textarea');
        if (ta) ta.style[prop] = val;
    }
}

function replaceSelectedImage(input) {
    if (!bookState.selectedEl || bookState.selectedEl.dataset.type !== 'image') return;
    if (!input.files || !input.files[0]) return;
    const reader = new FileReader();
    reader.onload = function(e) {
        const img = bookState.selectedEl.querySelector('img');
        if (img) img.src = e.target.result;
    };
    reader.readAsDataURL(input.files[0]);
    input.value = '';
}

function rgbToHex(rgb) {
    if (!rgb || rgb.startsWith('#')) return rgb;
    const res = rgb.match(/\d+/g);
    if (!res) return '#000';
    return '#' + res.slice(0,3).map(x => parseInt(x).toString(16).padStart(2,'0')).join('');
}

function addTextElement() {
    if (bookState.currentPage < 0) { showAdminToast('warning', 'Add a page first.'); return; }
    const canvas = document.getElementById('bookCanvas');
    const el = createDomElement({ type: 'text', left: '40px', top: '40px', text: 'Your text here', fontSize: '18px' });
    canvas.appendChild(el);
    selectElement(el);
}

function triggerImageUpload() {
    if (bookState.currentPage < 0) { showAdminToast('warning', 'Add a page first.'); return; }
    document.getElementById('bookImgUpload').click();
}

function addImageElement(input) {
    if (!input.files || !input.files[0]) return;
    const reader = new FileReader();
    reader.onload = function(e) {
        const canvas = document.getElementById('bookCanvas');
        const el = createDomElement({ type: 'image', left: '60px', top: '60px', src: e.target.result, width: '180px' });
        canvas.appendChild(el);
        selectElement(el);
    };
    reader.readAsDataURL(input.files[0]);
    input.value = '';
}

function deleteSelected() {
    if (bookState.selectedEl) { bookState.selectedEl.remove(); bookState.selectedEl = null; document.getElementById('bookElProps').style.display = 'none'; }
}

function bringFront() {
    if (!bookState.selectedEl) return;
    const max = Math.max(...[...document.querySelectorAll('.book-el')].map(e => parseInt(e.style.zIndex)||1));
    bookState.selectedEl.style.zIndex = max + 1;
}

function sendBack() {
    if (!bookState.selectedEl) return;
    const min = Math.min(...[...document.querySelectorAll('.book-el')].map(e => parseInt(e.style.zIndex)||1));
    bookState.selectedEl.style.zIndex = Math.max(1, min - 1);
}

function applyLayout(layout) {
    if (bookState.currentPage < 0) return;
    const canvas = document.getElementById('bookCanvas');
    canvas.innerHTML = '';
    const page = bookState.pages[bookState.currentPage];
    page.layout = layout;
    if (layout === 'text-left') {
        canvas.appendChild(createDomElement({ type:'text', left:'30px', top:'40px', text:'Heading text\n\nBody paragraph here.', fontSize:'20px', width:'240px' }));
    } else if (layout === 'text-right') {
        canvas.appendChild(createDomElement({ type:'text', left:'280px', top:'40px', text:'Heading\n\nBody text here.', fontSize:'20px', width:'240px' }));
    } else if (layout === 'split') {
        canvas.appendChild(createDomElement({ type:'text', left:'20px', top:'40px', text:'Title\n\nDescription here.', fontSize:'18px', width:'220px' }));
        canvas.appendChild(createDomElement({ type:'image', left:'280px', top:'40px', src:'', width:'180px' }));
    }
    renderPageList();
}

function saveBookPages() {
    saveCurrentPageState();
    localStorage.setItem('heroBookPages', JSON.stringify(bookState.pages));
    showAdminToast('success', 'Book pages saved to local storage. ' + bookState.pages.length + ' page(s).');
}

function previewBook() {
    saveCurrentPageState();
    const pages = bookState.pages;
    let html = '<div style="background:#f4f4f4; padding:10px; border-radius:8px;">';
    pages.forEach((page, i) => {
        html += `<div style="border:1px solid #ddd;padding:0;margin-bottom:1rem;background:${page.bg||'#fff'};border-radius:6px; overflow:hidden; position:relative; min-height:500px; width:100%; box-shadow:0 4px 10px rgba(0,0,0,0.1);">`;
        (page.elements||[]).forEach(el => {
            const z = el.zIndex || 1;
            const l = el.left || '0px';
            const t = el.top || '0px';
            const w = el.width || 'auto';
            const h = el.height || 'auto';
            
            if (el.type==='text') {
                const ff = el.fontFamily || "'Inter', sans-serif";
                const fs = el.fontSize || '18px';
                const col = el.color || '#333';
                const fw = el.fontWeight || '400';
                const fst = el.fontStyle || 'normal';
                const lh = el.lineHeight || '1.5';
                const txt = (el.text||'').replace(/\n/g,'<br>');
                html += `<div style="position:absolute; left:${l}; top:${t}; z-index:${z}; width:${w}; height:${h}; font-family:${ff}; font-size:${fs}; color:${col}; font-weight:${fw}; font-style:${fst}; line-height:${lh};">${txt}</div>`;
            }
            if (el.type==='image' && el.src) {
                html += `<div style="position:absolute; left:${l}; top:${t}; z-index:${z}; width:${w}; height:${h};"><img src="${el.src}" style="width:100%; height:100%; object-fit:cover; pointer-events:none;"></div>`;
            }
        });
        html += '</div>';
    });
    html += '</div>';
    document.getElementById('billPrintArea').innerHTML = html || '<p class="text-muted">No pages added yet.</p>';
    new bootstrap.Modal(document.getElementById('billModal')).show();
}

// Init book editor when tab is activated
document.addEventListener('DOMContentLoaded', () => {
    initBookEditor();
});


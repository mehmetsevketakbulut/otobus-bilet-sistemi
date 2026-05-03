// ── Protected API helper with Bearer token ──
function adminFetch(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return fetch(`${typeof API_BASE_URL !== 'undefined' ? API_BASE_URL : '/api'}${endpoint}`, { ...options, headers })
        .then(r => { if (!r.ok) throw new Error(r.statusText); return r.json(); });
}

document.addEventListener('DOMContentLoaded', () => {
    // ── Demo Data ──
    const companies = [
        { id:1, name:'Kamil Koç', taxNo:'1234567890', trips:42, date:'2024-01-15', active:true },
        { id:2, name:'Metro Turizm', taxNo:'9876543210', trips:38, date:'2024-03-01', active:true },
        { id:3, name:'Pamukkale', taxNo:'5678901234', trips:31, date:'2024-02-20', active:true },
        { id:4, name:'Süha Turizm', taxNo:'3456789012', trips:15, date:'2024-06-10', active:false },
        { id:5, name:'Niğde Lüks', taxNo:'7890123456', trips:22, date:'2024-04-05', active:true },
        { id:6, name:'Ulusoy', taxNo:'2345678901', trips:28, date:'2024-01-20', active:true },
    ];
    const users = [
        { name:'Ahmet Yılmaz', email:'ahmet@mail.com', role:'USER', date:'2025-11-03', tickets:12 },
        { name:'Ayşe Demir', email:'ayse@mail.com', role:'USER', date:'2025-12-15', tickets:8 },
        { name:'Mehmet Kaya', email:'mehmet@mail.com', role:'COMPANY', date:'2025-09-20', tickets:0 },
        { name:'Fatma Çelik', email:'fatma@mail.com', role:'USER', date:'2026-01-08', tickets:5 },
        { name:'Ali Öztürk', email:'ali@mail.com', role:'USER', date:'2026-02-14', tickets:21 },
        { name:'Zeynep Arslan', email:'zeynep@mail.com', role:'ADMIN', date:'2024-06-01', tickets:0 },
    ];
    const terminals = [
        { city:'İstanbul', name:'Esenler Otogarı' },{ city:'İstanbul', name:'Harem Otogarı' },
        { city:'Ankara', name:'AŞTİ' },{ city:'İzmir', name:'İzmir Otogarı' },
        { city:'Antalya', name:'Antalya Otogarı' },{ city:'Bursa', name:'Bursa Terminal' },
        { city:'Konya', name:'Konya Otogarı' },{ city:'Trabzon', name:'Trabzon Otogarı' },
    ];

    // ── Render Companies ──
    function renderCompanies(data) {
        const tb = document.getElementById('compTable');
        tb.innerHTML = data.map(c => {
            const dotColor = c.active ? 'bg-emerald-400' : 'bg-red-400';
            const label = c.active ? 'Aktif' : 'Pasif';
            const d = new Date(c.date);
            return `<tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3.5"><div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 font-bold text-xs">${c.name.substring(0,2).toUpperCase()}</div>
                    <span class="font-semibold text-white">${c.name}</span>
                </div></td>
                <td class="px-6 py-3.5 font-mono text-xs text-gray-500">${c.taxNo}</td>
                <td class="px-6 py-3.5 text-gray-400">${c.trips}</td>
                <td class="px-6 py-3.5 text-gray-500 text-xs">${d.toLocaleDateString('tr-TR')}</td>
                <td class="px-6 py-3.5">
                    <button onclick="toggleCompany(${c.id})" class="toggle-btn flex items-center gap-2 cursor-pointer" data-id="${c.id}">
                        <div class="toggle-track w-10 h-5 rounded-full ${c.active?'bg-emerald-500':'bg-gray-700'} relative">
                            <div class="toggle-dot w-4 h-4 rounded-full bg-white absolute top-0.5 ${c.active?'left-5.5':'left-0.5'}" style="left:${c.active?'22px':'2px'}"></div>
                        </div>
                        <span class="text-xs font-semibold ${c.active?'text-emerald-400':'text-gray-500'}">${label}</span>
                    </button>
                </td>
                <td class="px-6 py-3.5 text-right">
                    <button onclick="banCompany(${c.id})" class="ban-btn-${c.id} px-3 py-1.5 text-xs font-bold rounded-lg ${c.active?'bg-red-500/10 text-red-400 hover:bg-red-500/20':'bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20'} transition cursor-pointer">
                        ${c.active?'Banla':'Aktif Et'}
                    </button>
                </td>
            </tr>`;
        }).join('');
    }
    renderCompanies(companies);

    // Search
    const cs = document.getElementById('compSearch');
    if (cs) cs.addEventListener('input', e => {
        renderCompanies(companies.filter(c => c.name.toLowerCase().includes(e.target.value.toLowerCase())));
    });

    // ── Toggle / Ban company ──
    window.toggleCompany = (id) => {
        const c = companies.find(x => x.id === id);
        if (c) { c.active = !c.active; renderCompanies(companies); }
        // Production: adminFetch(`/admin/companies/${id}/toggle`, { method: 'PATCH' });
    };
    window.banCompany = (id) => {
        const c = companies.find(x => x.id === id);
        if (!c) return;
        const action = c.active ? 'banlamak' : 'aktif etmek';
        if (!confirm(`${c.name} firmasını ${action} istediğinize emin misiniz?`)) return;
        c.active = !c.active;
        renderCompanies(companies);
        // Production: adminFetch(`/admin/companies/${id}/ban`, { method: 'PATCH' });
    };

    // ── Render Users ──
    document.getElementById('userTable').innerHTML = users.map(u => {
        const roleBg = u.role==='ADMIN' ? 'bg-indigo-500/15 text-indigo-400' : u.role==='COMPANY' ? 'bg-amber-500/15 text-amber-400' : 'bg-emerald-500/15 text-emerald-400';
        return `<tr class="tbl-row border-b border-white/5">
            <td class="px-6 py-3.5 font-semibold text-white">${u.name}</td>
            <td class="px-6 py-3.5 text-gray-500 text-xs">${u.email}</td>
            <td class="px-6 py-3.5"><span class="px-2 py-0.5 text-[10px] font-bold rounded-md ${roleBg}">${u.role}</span></td>
            <td class="px-6 py-3.5 text-gray-500 text-xs">${new Date(u.date).toLocaleDateString('tr-TR')}</td>
            <td class="px-6 py-3.5 text-gray-400">${u.tickets}</td>
        </tr>`;
    }).join('');

    // ── Render Terminals ──
    function renderTerminals() {
        document.getElementById('termTable').innerHTML = terminals.map((t,i) => `
            <tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3 font-semibold text-white">${t.city}</td>
                <td class="px-6 py-3 text-gray-400">${t.name}</td>
                <td class="px-6 py-3 text-right"><button onclick="removeTerm(${i})" class="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-600 hover:text-red-400 transition cursor-pointer"><svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg></button></td>
            </tr>`).join('');
    }
    renderTerminals();

    // Add terminal
    document.getElementById('termForm').addEventListener('submit', e => {
        e.preventDefault();
        terminals.push({ city: document.getElementById('termCity').value.trim(), name: document.getElementById('termName').value.trim() });
        renderTerminals();
        e.target.reset();
        // Production: adminFetch('/admin/terminals', { method: 'POST', body: JSON.stringify({...}) });
    });
    window.removeTerm = (i) => { if(confirm('Bu terminali silmek istediğinize emin misiniz?')){terminals.splice(i,1);renderTerminals();} };

    // ── Sidebar Nav ──
    const pages = { dashboard:'Gösterge Paneli', companies:'Firmalar', users:'Kullanıcılar', terminals:'Terminaller', settings:'Sistem Ayarları' };
    document.querySelectorAll('.sb-link').forEach(btn => {
        btn.addEventListener('click', () => {
            const pg = btn.dataset.page;
            document.querySelectorAll('.sb-link').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('pageTitle').textContent = pages[pg];
            Object.keys(pages).forEach(p => {
                const el = document.getElementById('pg' + p.charAt(0).toUpperCase() + p.slice(1));
                if (el) el.classList.toggle('hidden', p !== pg);
            });
            document.getElementById('sidebar').classList.add('-translate-x-full');
            document.getElementById('sbOverlay').classList.add('hidden');
        });
    });
});

function toggleSB() {
    document.getElementById('sidebar').classList.toggle('-translate-x-full');
    document.getElementById('sbOverlay').classList.toggle('hidden');
}

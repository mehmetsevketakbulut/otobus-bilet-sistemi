document.addEventListener('DOMContentLoaded', () => {
    // ── Demo trip data ──
    const trips = [
        { id:1, from:'İstanbul', to:'Ankara', date:'2026-05-04', time:'08:00', bus:'34 KK 001', busType:'2+1', price:450, sold:28, capacity:37, status:'active' },
        { id:2, from:'İstanbul', to:'İzmir', date:'2026-05-04', time:'10:30', bus:'34 KK 002', busType:'2+1', price:380, sold:35, capacity:40, status:'active' },
        { id:3, from:'İstanbul', to:'Antalya', date:'2026-05-04', time:'14:00', bus:'34 KK 003', busType:'2+2', price:520, sold:40, capacity:48, status:'active' },
        { id:4, from:'Ankara', to:'İstanbul', date:'2026-05-04', time:'16:00', bus:'34 KK 001', busType:'2+1', price:450, sold:22, capacity:37, status:'active' },
        { id:5, from:'İzmir', to:'İstanbul', date:'2026-05-03', time:'22:00', bus:'34 KK 002', busType:'2+1', price:380, sold:40, capacity:40, status:'completed' },
        { id:6, from:'İstanbul', to:'Bursa', date:'2026-05-05', time:'09:00', bus:'34 KK 003', busType:'2+2', price:250, sold:0, capacity:48, status:'active' },
    ];

    const cities = ['İstanbul','Ankara','İzmir','Antalya','Bursa','Eskişehir','Konya','Trabzon','Samsun','Adana','Mersin','Kayseri','Diyarbakır','Erzurum','Gaziantep'];

    // ── Fill modal dropdowns ──
    const fromSel = document.getElementById('modalFrom');
    const toSel = document.getElementById('modalTo');
    cities.forEach(c => {
        fromSel.innerHTML += `<option value="${c}">${c}</option>`;
        toSel.innerHTML += `<option value="${c}">${c}</option>`;
    });

    // Set min date to today
    const dateInput = document.getElementById('modalDate');
    dateInput.min = new Date().toISOString().split('T')[0];

    // ── Render trips table ──
    function renderTable(data) {
        const tbody = document.getElementById('tripsTableBody');
        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-8 text-center text-gray-400 text-sm">Henüz sefer bulunmuyor.</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(t => {
            const pct = Math.round((t.sold / t.capacity) * 100);
            const pctColor = pct > 80 ? 'bg-emerald-500' : pct > 50 ? 'bg-amber-400' : 'bg-blue-400';
            const statusBadge = t.status === 'active'
                ? '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-emerald-50 text-emerald-700">Aktif</span>'
                : t.status === 'cancelled'
                ? '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-red-50 text-red-600">İptal</span>'
                : '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-gray-100 text-gray-500">Tamamlandı</span>';
            const d = new Date(t.date);
            const dateStr = d.toLocaleDateString('tr-TR',{day:'numeric',month:'short'});
            return `
            <tr class="table-row border-b border-gray-50">
                <td class="px-6 py-3.5">
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-navy-800">${t.from}</span>
                        <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
                        <span class="font-bold text-navy-800">${t.to}</span>
                    </div>
                </td>
                <td class="px-6 py-3.5 text-gray-600">${dateStr} <span class="font-semibold text-navy-800">${t.time}</span></td>
                <td class="px-6 py-3.5"><span class="text-xs bg-gray-100 px-2 py-0.5 rounded-md font-medium text-gray-600">${t.bus} · ${t.busType}</span></td>
                <td class="px-6 py-3.5 font-bold text-navy-800">${t.price} ₺</td>
                <td class="px-6 py-3.5">
                    <div class="flex items-center gap-2">
                        <div class="w-16 h-1.5 bg-gray-200 rounded-full overflow-hidden"><div class="${pctColor} h-full rounded-full" style="width:${pct}%"></div></div>
                        <span class="text-xs text-gray-500">${t.sold}/${t.capacity}</span>
                    </div>
                </td>
                <td class="px-6 py-3.5">${statusBadge}</td>
                <td class="px-6 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-1">
                        <button class="p-1.5 rounded-lg hover:bg-blue-50 text-gray-400 hover:text-blue-600 transition cursor-pointer" title="Düzenle">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                        </button>
                        <button onclick="cancelTrip(${t.id})" class="p-1.5 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-600 transition cursor-pointer" title="İptal Et">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M6 18L18 6M6 6l12 12"/></svg>
                        </button>
                    </div>
                </td>
            </tr>`;
        }).join('');
    }
    renderTable(trips);

    // ── Search filter ──
    document.getElementById('tripSearch').addEventListener('input', e => {
        const q = e.target.value.toLowerCase();
        renderTable(trips.filter(t => `${t.from} ${t.to}`.toLowerCase().includes(q)));
    });

    // ── Sidebar navigation ──
    const pages = { dashboard:'Gösterge Paneli', trips:'Seferlerim', buses:'Otobüslerim', reports:'Satış Raporları' };
    document.querySelectorAll('.sidebar-link').forEach(btn => {
        btn.addEventListener('click', () => {
            const page = btn.dataset.page;
            document.querySelectorAll('.sidebar-link').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('pageTitle').textContent = pages[page];
            Object.keys(pages).forEach(p => {
                const el = document.getElementById('page' + p.charAt(0).toUpperCase() + p.slice(1));
                if (el) el.classList.toggle('hidden', p !== page);
            });
            // Close mobile sidebar
            document.getElementById('sidebar').classList.add('-translate-x-full');
            document.getElementById('sidebarOverlay').classList.add('hidden');
        });
    });

    // ── Modal ──
    document.getElementById('addTripBtn').addEventListener('click', () => {
        document.getElementById('tripModal').classList.remove('hidden');
    });

    document.getElementById('addTripForm').addEventListener('submit', e => {
        e.preventDefault();
        const newTrip = {
            id: trips.length + 1,
            from: document.getElementById('modalFrom').value,
            to: document.getElementById('modalTo').value,
            date: document.getElementById('modalDate').value,
            time: document.getElementById('modalTime').value,
            bus: document.getElementById('modalBus').selectedOptions[0]?.text.split('—')[0]?.trim() || '-',
            busType: document.getElementById('modalBus').selectedOptions[0]?.text.includes('2+1') ? '2+1' : '2+2',
            price: parseInt(document.getElementById('modalPrice').value),
            sold: 0,
            capacity: document.getElementById('modalBus').selectedOptions[0]?.text.includes('48') ? 48 : 37,
            status: 'active'
        };
        trips.unshift(newTrip);
        renderTable(trips);
        closeModal();
        e.target.reset();
    });

    // ── Logout ──
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });
});

// ── Global helpers ──
function toggleSidebar() {
    const sb = document.getElementById('sidebar');
    const ov = document.getElementById('sidebarOverlay');
    sb.classList.toggle('-translate-x-full');
    ov.classList.toggle('hidden');
}

function closeModal() {
    document.getElementById('tripModal').classList.add('hidden');
}

function cancelTrip(id) {
    if (!confirm('Bu seferi iptal etmek istediğinize emin misiniz?')) return;
    // In production: API call. Demo: just update UI
    const row = event.target.closest('tr');
    if (row) {
        const statusCell = row.querySelectorAll('td')[5];
        if (statusCell) statusCell.innerHTML = '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-red-50 text-red-600">İptal</span>';
    }
}

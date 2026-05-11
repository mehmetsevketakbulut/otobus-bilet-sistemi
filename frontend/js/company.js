document.addEventListener('DOMContentLoaded', async () => {
    // ── State ──
    let terminals = [];
    let buses = [];
    let trips = [];
    let stopCounter = 0;

    // ── Load data from API ──
    try {
        terminals = await fetchApi('/terminals');
        buses = await fetchApi('/buses');
    } catch (e) {
        console.error('Veri yüklenemedi:', e);
    }

    // ── Fill modal dropdowns with real terminal data ──
    const fromSel = document.getElementById('modalFrom');
    const toSel = document.getElementById('modalTo');
    terminals.forEach(t => {
        const label = `${t.name} (${t.city?.name || ''})`;
        fromSel.innerHTML += `<option value="${t.id}">${label}</option>`;
        toSel.innerHTML += `<option value="${t.id}">${label}</option>`;
    });

    // ── Fill bus dropdown from API ──
    const busSel = document.getElementById('modalBus');
    buses.forEach(b => {
        busSel.innerHTML += `<option value="${b.id}">${b.plate} — ${b.seatCapacity} Koltuk</option>`;
    });

    // Set min date to today
    const dateInput = document.getElementById('modalDate');
    dateInput.min = new Date().toISOString().split('T')[0];

    // ── Load existing trips ──
    await loadTrips();

    async function loadTrips() {
        try {
            const allTrips = await fetchApi('/trips/admin/pending');
            trips = allTrips || [];
            renderTable(trips);
        } catch (e) {
            // Fallback: Try fetching all trips
            try {
                const resp = await fetch(`${API_BASE_URL}/trips/admin/pending`, {
                    headers: { 'Authorization': `Bearer ${getToken()}` }
                });
                if (resp.ok) {
                    trips = await resp.json();
                } else {
                    trips = [];
                }
            } catch {
                trips = [];
            }
            renderTable(trips);
        }
    }

    // ── Render trips table ──
    function renderTable(data) {
        const tbody = document.getElementById('tripsTableBody');
        if (!data || !data.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-6 py-8 text-center text-gray-400 text-sm">Henüz sefer bulunmuyor.</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(t => {
            const fromName = t.kalkisTerminali?.city?.name || t.kalkisTerminali?.name || '-';
            const toName = t.varisTerminali?.city?.name || t.varisTerminali?.name || '-';
            const busPlate = t.otobus?.plate || '-';
            const seatCap = t.otobus?.seatCapacity || '-';
            const price = t.fiyat || 0;
            const dateStr = t.kalkisSaati ? new Date(t.kalkisSaati).toLocaleString('tr-TR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-';
            const stopCount = t.stops ? t.stops.length : 0;
            const statusBadge = t.approved
                ? '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-emerald-50 text-emerald-700">Onaylandı</span>'
                : '<span class="px-2 py-0.5 text-[10px] font-bold rounded-md bg-amber-50 text-amber-600">Onay Bekliyor</span>';
            return `
            <tr class="table-row border-b border-gray-50">
                <td class="px-6 py-3.5">
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-navy-800">${fromName}</span>
                        <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
                        <span class="font-bold text-navy-800">${toName}</span>
                        ${stopCount > 2 ? `<span class="text-[10px] bg-blue-50 text-blue-600 px-1.5 py-0.5 rounded font-medium">${stopCount - 2} ara durak</span>` : ''}
                    </div>
                </td>
                <td class="px-6 py-3.5 text-gray-600">${dateStr}</td>
                <td class="px-6 py-3.5"><span class="text-xs bg-gray-100 px-2 py-0.5 rounded-md font-medium text-gray-600">${busPlate} · ${seatCap} koltuk</span></td>
                <td class="px-6 py-3.5 font-bold text-navy-800">${price} ₺</td>
                <td class="px-6 py-3.5">
                    <span class="text-xs text-gray-500">—</span>
                </td>
                <td class="px-6 py-3.5">${statusBadge}</td>
                <td class="px-6 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-1">
                        <button class="p-1.5 rounded-lg hover:bg-blue-50 text-gray-400 hover:text-blue-600 transition cursor-pointer" title="Düzenle">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
                        </button>
                    </div>
                </td>
            </tr>`;
        }).join('');
    }

    // ── Search filter ──
    document.getElementById('tripSearch').addEventListener('input', e => {
        const q = e.target.value.toLowerCase();
        const filtered = trips.filter(t => {
            const from = t.kalkisTerminali?.city?.name || t.kalkisTerminali?.name || '';
            const to = t.varisTerminali?.city?.name || t.varisTerminali?.name || '';
            return `${from} ${to}`.toLowerCase().includes(q);
        });
        renderTable(filtered);
    });

    // ══════════════════════════════════════════════
    // ══  ARA DURAK (INTERMEDIATE STOP) YÖNETİMİ ══
    // ══════════════════════════════════════════════

    document.getElementById('addStopBtn').addEventListener('click', () => {
        addStopRow();
    });

    function addStopRow() {
        stopCounter++;
        const container = document.getElementById('stopsContainer');
        document.getElementById('noStopsMessage').classList.add('hidden');

        const row = document.createElement('div');
        row.className = 'stop-row bg-gray-50 rounded-xl p-3 border border-gray-100 animate-in';
        row.dataset.stopId = stopCounter;
        row.innerHTML = `
            <div class="flex items-center justify-between mb-2">
                <span class="text-xs font-bold text-navy-700">🔹 Ara Durak #${stopCounter}</span>
                <button type="button" class="remove-stop-btn p-1 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-500 transition cursor-pointer" title="Durağı Kaldır">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                </button>
            </div>
            <div class="grid grid-cols-3 gap-3">
                <div>
                    <label class="block text-[10px] font-semibold text-gray-500 mb-0.5">Terminal</label>
                    <select class="stop-terminal input-f w-full px-2 py-2 border-2 border-gray-200 rounded-lg text-xs outline-none" required>
                        <option value="" disabled selected>Terminal seçin</option>
                        ${terminals.map(t => `<option value="${t.id}">${t.name} (${t.city?.name || ''})</option>`).join('')}
                    </select>
                </div>
                <div>
                    <label class="block text-[10px] font-semibold text-gray-500 mb-0.5">Varış Saati</label>
                    <input type="time" class="stop-time input-f w-full px-2 py-2 border-2 border-gray-200 rounded-lg text-xs outline-none" required>
                </div>
                <div>
                    <label class="block text-[10px] font-semibold text-gray-500 mb-0.5">Fiyat (Kalkıştan itibaren, ₺)</label>
                    <input type="number" class="stop-price input-f w-full px-2 py-2 border-2 border-gray-200 rounded-lg text-xs outline-none" min="0" placeholder="Ör: 200" required>
                </div>
            </div>
        `;

        container.appendChild(row);

        // Remove button handler
        row.querySelector('.remove-stop-btn').addEventListener('click', () => {
            row.remove();
            const remaining = document.querySelectorAll('.stop-row');
            if (remaining.length === 0) {
                document.getElementById('noStopsMessage').classList.remove('hidden');
            }
            updateRoutePreview();
        });

        // Update preview on change
        row.querySelectorAll('select, input').forEach(el => {
            el.addEventListener('change', updateRoutePreview);
        });

        updateRoutePreview();
    }

    function updateRoutePreview() {
        const fromId = document.getElementById('modalFrom').value;
        const toId = document.getElementById('modalTo').value;
        const preview = document.getElementById('routePreview');
        const content = document.getElementById('routePreviewContent');

        if (!fromId && !toId) {
            preview.classList.add('hidden');
            return;
        }

        preview.classList.remove('hidden');
        let routeHtml = '';

        // Kalkış
        if (fromId) {
            const fromTerminal = terminals.find(t => t.id == fromId);
            routeHtml += `<span class="bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-md font-bold">🚀 ${fromTerminal?.city?.name || fromTerminal?.name || 'Kalkış'}</span>`;
        }

        // Ara Duraklar
        const stopRows = document.querySelectorAll('.stop-row');
        stopRows.forEach(row => {
            const terminalId = row.querySelector('.stop-terminal')?.value;
            if (terminalId) {
                const terminal = terminals.find(t => t.id == terminalId);
                routeHtml += `<svg class="w-4 h-4 text-gray-300" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>`;
                routeHtml += `<span class="bg-blue-100 text-blue-700 px-2 py-0.5 rounded-md font-medium">🚏 ${terminal?.city?.name || terminal?.name || '?'}</span>`;
            }
        });

        // Varış
        if (toId) {
            const toTerminal = terminals.find(t => t.id == toId);
            routeHtml += `<svg class="w-4 h-4 text-gray-300" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>`;
            routeHtml += `<span class="bg-red-100 text-red-700 px-2 py-0.5 rounded-md font-bold">🏁 ${toTerminal?.city?.name || toTerminal?.name || 'Varış'}</span>`;
        }

        content.innerHTML = routeHtml;
    }

    // Update preview when departure/arrival changes
    document.getElementById('modalFrom').addEventListener('change', updateRoutePreview);
    document.getElementById('modalTo').addEventListener('change', updateRoutePreview);

    // ── Sidebar navigation ──
    const pages = { dashboard: 'Gösterge Paneli', trips: 'Seferlerim', buses: 'Otobüslerim', reports: 'Satış Raporları' };
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

    // ══════════════════════════════════════
    // ══  SEFER OLUŞTURMA (FORM SUBMIT)  ══
    // ══════════════════════════════════════
    document.getElementById('addTripForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const fromTerminalId = parseInt(document.getElementById('modalFrom').value);
        const toTerminalId = parseInt(document.getElementById('modalTo').value);
        const busId = parseInt(document.getElementById('modalBus').value);
        const date = document.getElementById('modalDate').value;
        const time = document.getElementById('modalTime').value;
        const totalPrice = parseFloat(document.getElementById('modalPrice').value);

        if (fromTerminalId === toTerminalId) {
            alert('Kalkış ve varış terminali aynı olamaz!');
            return;
        }

        // ═══════════════════════════════════════════════════════════════
        // ══  GECE YARISI GEÇİŞ HESAPLAMASI (Overnight Trip Support) ══
        // ═══════════════════════════════════════════════════════════════
        // Mantık: Kalkış tarih+saatinden başlayarak, her sonraki durakta
        // saatin bir öncekinden küçük olup olmadığını kontrol ediyoruz.
        // Eğer küçükse → gece yarısını geçmiş demektir → tarihi 1 gün ileri atıyoruz.

        /**
         * Akıllı tarih hesaplama fonksiyonu.
         * @param {string} baseDate   - Başlangıç tarihi (YYYY-MM-DD)
         * @param {string} timeStr    - Durağın saati (HH:mm)
         * @param {string} prevTimeStr - Önceki durağın saati (HH:mm)
         * @param {number} dayOffset  - Şu ana kadar kaç gün ileri gidildiği
         * @returns {{ dateTime: string, newDayOffset: number }}
         */
        function calculateDateTime(baseDate, timeStr, prevTimeStr, dayOffset) {
            const [currH, currM] = timeStr.split(':').map(Number);
            const [prevH, prevM] = prevTimeStr.split(':').map(Number);

            // Eğer mevcut saat öncekinden küçükse → gece yarısını geçtik
            if (currH < prevH || (currH === prevH && currM < prevM)) {
                dayOffset++;
            }

            // Tarihi dayOffset kadar ileri al
            const dateObj = new Date(`${baseDate}T00:00:00`);
            dateObj.setDate(dateObj.getDate() + dayOffset);
            const adjustedDate = dateObj.toISOString().split('T')[0]; // YYYY-MM-DD

            return {
                dateTime: `${adjustedDate}T${String(currH).padStart(2, '0')}:${String(currM).padStart(2, '0')}:00`,
                newDayOffset: dayOffset
            };
        }

        // Kalkış saatini oluştur
        const departureDateTime = `${date}T${time}:00`;

        // ═══ STOPS DİZİSİNİ OLUŞTUR ═══
        const stops = [];
        let dayOffset = 0;       // Gece yarısını kaç kez geçtiğimizi takip eder
        let previousTime = time; // Bir önceki durağın saati

        // 1) Kalkış durağı (stopOrder = 1, priceFromStart = 0)
        stops.push({
            terminal: { id: fromTerminalId },
            stopOrder: 1,
            departureTime: departureDateTime,
            priceFromStart: 0
        });

        // 2) Ara duraklar (stopOrder = 2, 3, 4...)
        const stopRows = document.querySelectorAll('.stop-row');
        let order = 2;
        for (const row of stopRows) {
            const terminalId = parseInt(row.querySelector('.stop-terminal').value);
            const stopTime = row.querySelector('.stop-time').value;
            const stopPrice = parseFloat(row.querySelector('.stop-price').value);

            if (!terminalId || !stopTime || isNaN(stopPrice)) {
                alert('Lütfen tüm ara durak bilgilerini eksiksiz doldurun.');
                return;
            }

            // Gece yarısı geçişini otomatik hesapla
            const calc = calculateDateTime(date, stopTime, previousTime, dayOffset);
            dayOffset = calc.newDayOffset;
            previousTime = stopTime;

            stops.push({
                terminal: { id: terminalId },
                stopOrder: order,
                departureTime: calc.dateTime,
                priceFromStart: stopPrice
            });
            order++;
        }

        // 3) Varış durağı — varış saatini de gece yarısı geçişine göre hesapla
        const arrivalTimeInput = document.getElementById('modalArrivalTime').value;
        if (!arrivalTimeInput) {
            alert('Lütfen tahmini varış saatini giriniz.');
            return;
        }
        const arrivalCalc = calculateDateTime(date, arrivalTimeInput, previousTime, dayOffset);

        stops.push({
            terminal: { id: toTerminalId },
            stopOrder: order,
            departureTime: arrivalCalc.dateTime,
            priceFromStart: totalPrice
        });

        // ═══ API'YE GÖNDER ═══
        const tripData = {
            kalkisTerminali: { id: fromTerminalId },
            varisTerminali: { id: toTerminalId },
            otobus: { id: busId },
            kalkisSaati: departureDateTime,
            fiyat: totalPrice,
            stops: stops
        };

        try {
            const submitBtn = e.target.querySelector('button[type="submit"]');
            submitBtn.disabled = true;
            submitBtn.textContent = 'Oluşturuluyor...';

            await fetchApi('/trips/company', {
                method: 'POST',
                body: JSON.stringify(tripData)
            });

            alert('✅ Sefer başarıyla oluşturuldu! Admin onayı bekleniyor.');
            closeModal();
            e.target.reset();

            // Durakları temizle
            document.getElementById('stopsContainer').innerHTML = '';
            document.getElementById('noStopsMessage').classList.remove('hidden');
            document.getElementById('routePreview').classList.add('hidden');
            stopCounter = 0;

            // Tabloyu yenile
            await loadTrips();

            submitBtn.disabled = false;
            submitBtn.textContent = 'Sefer Oluştur';
        } catch (err) {
            alert('❌ Hata: ' + (err.message || 'Sefer oluşturulamadı!'));
            const submitBtn = e.target.querySelector('button[type="submit"]');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Sefer Oluştur';
        }
    });

    // ── Logout ──
    document.getElementById('logoutBtn').addEventListener('click', () => {
        localStorage.removeItem('jwt_token');
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

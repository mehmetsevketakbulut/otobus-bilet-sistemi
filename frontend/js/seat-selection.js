/**
 * Koltuk Seçim Modülü
 * 2+1 otobüs düzeni — Türk otobüs sistemine uygun
 * Backend format: [{seatNo, status, gender}]
 */
const SeatSelection = (() => {
    let activePanel = null;   // currently open panel element
    let activeTripId = null;  // currently open trip id
    let selectedSeats = [];   // [{seatNo, gender}]

    let currentFromStopId = null;
    let currentToStopId = null;

    // ── Koltuk verilerini API'den çek ──
    async function fetchSeats(tripId, fromStopId, toStopId) {
        try {
            const data = await fetchApi(`/trips/${tripId}/seats?fromStopId=${fromStopId}&toStopId=${toStopId}`, { method: 'GET' });
            return data; // [{seatNo, status, gender}, …]
        } catch (e) {
            console.warn('Koltuk API erişilemedi, demo veri:', e);
            return generateDemoSeats();
        }
    }

    function generateDemoSeats() {
        const seats = [];
        for (let i = 1; i <= 37; i++) {
            const rand = Math.random();
            if (rand < 0.35) {
                seats.push({ seatNo: i, status: 'occupied', gender: Math.random() > 0.5 ? 'male' : 'female' });
            } else {
                seats.push({ seatNo: i, status: 'available', gender: null });
            }
        }
        return seats;
    }

    // ── Ana açma/kapama ──
    async function toggle(tripId, busType, anchorEl, fromStopId, toStopId) {
        // Zaten açıksa kapat
        if (activeTripId === tripId && activePanel) {
            close();
            return;
        }
        // Öncekini kapat
        close();

        activeTripId = tripId;
        currentFromStopId = fromStopId;
        currentToStopId = toStopId;
        selectedSeats = [];

        const seats = await fetchSeats(tripId, fromStopId, toStopId);
        const panel = buildPanel(seats, busType, tripId);
        anchorEl.insertAdjacentElement('afterend', panel);
        activePanel = panel;

        // Animasyon
        requestAnimationFrame(() => panel.classList.add('seat-panel-open'));
    }

    function close() {
        if (activePanel) {
            activePanel.classList.remove('seat-panel-open');
            setTimeout(() => { activePanel?.remove(); }, 300);
            activePanel = null;
            activeTripId = null;
            selectedSeats = [];
        }
        closeGenderPopup();
    }

    // ── Panel HTML ──
    function buildPanel(seats, busType, tripId) {
        const is2plus1 = (busType || '2+1') === '2+1';
        const colsRight = is2plus1 ? 1 : 2;
        const seatsPerRow = is2plus1 ? 3 : 4;
        const totalRows = Math.ceil(seats.length / seatsPerRow);

        const panel = document.createElement('div');
        panel.className = 'seat-panel bg-white rounded-2xl border border-gray-200 shadow-xl mt-4 overflow-hidden';
        panel.id = `seat-panel-${tripId}`;

        // Başlık
        let html = `
        <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100 bg-gray-50/80">
            <div>
                <h3 class="text-base font-bold text-navy-800">Koltuk Seçimi</h3>
                <p class="text-xs text-gray-500 mt-0.5">Boş koltuğa tıklayıp cinsiyet seçin</p>
            </div>
            <button onclick="SeatSelection.close()" class="p-2 rounded-lg hover:bg-gray-200 transition cursor-pointer" title="Kapat">
                <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
        </div>`;

        html += `<div class="flex flex-col lg:flex-row">`;

        // ── Sol: Otobüs şeması ──
        html += `<div class="flex-1 p-5 flex justify-center">`;
        html += `<div class="bus-body relative bg-gray-50 rounded-2xl border-2 border-gray-200 p-4 pt-6 inline-block">`;

        // Direksiyon
        html += `
        <div class="flex items-center justify-end mb-3 pr-1">
            <div class="flex items-center gap-2 text-xs text-gray-400 font-semibold">
                <span>ÖN</span>
                <div class="w-9 h-9 rounded-full border-2 border-gray-300 flex items-center justify-center bg-white">
                    <svg class="w-5 h-5 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="12" cy="12" r="9"/>
                        <circle cx="12" cy="12" r="3"/>
                        <path d="M12 3v6M12 15v6M3 12h6M15 12h6"/>
                    </svg>
                </div>
            </div>
        </div>`;

        // Koltuk satırları
        for (let row = 0; row < totalRows; row++) {
            html += `<div class="flex items-center gap-1 mb-1.5 justify-center">`;

            // Sol taraf (2 koltuk)
            for (let col = 0; col < 2; col++) {
                const idx = row * seatsPerRow + col;
                if (idx < seats.length) {
                    html += renderSeat(seats[idx]);
                } else {
                    html += `<div class="w-10 h-10"></div>`;
                }
            }

            // Koridor
            html += `<div class="w-6 h-10 flex items-center justify-center">
                <div class="w-px h-full bg-gray-200"></div>
            </div>`;

            // Sağ taraf (1 veya 2 koltuk)
            for (let col = 2; col < seatsPerRow; col++) {
                const idx = row * seatsPerRow + col;
                if (idx < seats.length) {
                    html += renderSeat(seats[idx]);
                } else {
                    html += `<div class="w-10 h-10"></div>`;
                }
            }

            html += `</div>`;
        }

        html += `</div></div>`; // bus-body, left section

        // ── Sağ: Seçim bilgisi + Lejant ──
        html += `
        <div class="lg:w-64 border-t lg:border-t-0 lg:border-l border-gray-100 p-5 flex flex-col gap-4 bg-white">
            <!-- Lejant -->
            <div>
                <p class="text-xs font-bold text-navy-800 uppercase tracking-wider mb-3">Renk Açıklaması</p>
                <div class="grid grid-cols-2 gap-2 text-xs">
                    <div class="flex items-center gap-2">
                        <div class="w-6 h-6 rounded-md bg-white border-2 border-gray-300"></div>
                        <span class="text-gray-600">Boş</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <div class="w-6 h-6 rounded-md bg-emerald-500"></div>
                        <span class="text-gray-600">Seçili</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <div class="w-6 h-6 rounded-md bg-blue-400"></div>
                        <span class="text-gray-600">Dolu (Erkek)</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <div class="w-6 h-6 rounded-md bg-pink-400"></div>
                        <span class="text-gray-600">Dolu (Kadın)</span>
                    </div>
                </div>
            </div>

            <!-- Seçilen koltuklar -->
            <div>
                <p class="text-xs font-bold text-navy-800 uppercase tracking-wider mb-2">Seçilen Koltuklar</p>
                <div id="selected-seats-list-${tripId}" class="text-sm text-gray-500 min-h-[40px]">
                    <p class="text-xs text-gray-400 italic">Henüz koltuk seçilmedi</p>
                </div>
            </div>

            <!-- Onayla Butonu -->
            <button id="confirm-seats-${tripId}" onclick="SeatSelection.confirm(${tripId})"
                class="mt-auto w-full py-3 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-600 hover:to-emerald-700 text-white font-bold text-sm rounded-xl shadow-lg transition-all cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed"
                disabled>
                Devam Et
            </button>
        </div>`;

        html += `</div>`; // flex row
        panel.innerHTML = html;
        return panel;
    }

    // ── Tek koltuk render ──
    function renderSeat(seat) {
        const no = seat.seatNo;
        const isOccupied = seat.status === 'occupied';
        const isMale = seat.gender === 'male';
        const isFemale = seat.gender === 'female';

        let bgClass, textClass, cursor, title, onclick;

        if (isOccupied && isMale) {
            bgClass = 'bg-blue-400 border-blue-500';
            textClass = 'text-white';
            cursor = 'cursor-not-allowed opacity-80';
            title = `Koltuk ${no} — Dolu (Erkek)`;
            onclick = '';
        } else if (isOccupied && isFemale) {
            bgClass = 'bg-pink-400 border-pink-500';
            textClass = 'text-white';
            cursor = 'cursor-not-allowed opacity-80';
            title = `Koltuk ${no} — Dolu (Kadın)`;
            onclick = '';
        } else {
            bgClass = 'bg-white border-gray-300 hover:border-navy-600 hover:bg-navy-50';
            textClass = 'text-gray-700';
            cursor = 'cursor-pointer';
            title = `Koltuk ${no} — Boş`;
            onclick = `onclick="SeatSelection.onSeatClick(event, ${no})"`;
        }

        return `
        <button type="button" id="seat-${no}" data-seat="${no}"
            class="seat-box w-10 h-10 rounded-lg border-2 ${bgClass} ${textClass} ${cursor} flex items-center justify-center text-xs font-bold transition-all duration-200 relative"
            title="${title}" ${onclick} ${isOccupied ? 'disabled' : ''}>
            ${no}
        </button>`;
    }

    // ── Koltuğa tıklama ──
    function onSeatClick(event, seatNo) {
        event.stopPropagation();
        const btn = document.getElementById(`seat-${seatNo}`);
        if (!btn) return;

        // Zaten seçiliyse kaldır
        const existingIdx = selectedSeats.findIndex(s => s.seatNo === seatNo);
        if (existingIdx !== -1) {
            selectedSeats.splice(existingIdx, 1);
            btn.className = btn.className
                .replace(/bg-emerald-500 border-emerald-600/g, 'bg-white border-gray-300 hover:border-navy-600 hover:bg-navy-50')
                .replace(/text-white/g, 'text-gray-700');
            updateSelectedList();
            closeGenderPopup();
            return;
        }

        // Cinsiyet popup aç
        showGenderPopup(btn, seatNo);
    }

    // ── Cinsiyet Popup ──
    function showGenderPopup(anchorBtn, seatNo) {
        closeGenderPopup();

        const popup = document.createElement('div');
        popup.id = 'gender-popup';
        popup.className = 'gender-popup absolute z-50';

        popup.innerHTML = `
        <div class="bg-white rounded-xl shadow-2xl border border-gray-200 p-3 w-44 animate-popup">
            <p class="text-xs font-bold text-navy-800 mb-2 text-center">Koltuk ${seatNo}</p>
            <p class="text-[10px] text-gray-400 text-center mb-3">Cinsiyet seçiniz</p>
            <div class="flex gap-2">
                <button onclick="SeatSelection.selectGender(${seatNo}, 'male')"
                    class="flex-1 py-2 rounded-lg bg-blue-50 hover:bg-blue-100 border-2 border-blue-200 hover:border-blue-400 text-blue-700 font-bold text-xs transition cursor-pointer flex flex-col items-center gap-1">
                    <span class="text-lg">👨</span> Erkek
                </button>
                <button onclick="SeatSelection.selectGender(${seatNo}, 'female')"
                    class="flex-1 py-2 rounded-lg bg-pink-50 hover:bg-pink-100 border-2 border-pink-200 hover:border-pink-400 text-pink-700 font-bold text-xs transition cursor-pointer flex flex-col items-center gap-1">
                    <span class="text-lg">👩</span> Kadın
                </button>
            </div>
            <button onclick="SeatSelection.closeGenderPopup()"
                class="w-full mt-2 text-[10px] text-gray-400 hover:text-gray-600 transition cursor-pointer text-center">İptal</button>
        </div>`;

        // Popup konumlandırma
        const rect = anchorBtn.getBoundingClientRect();
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

        popup.style.position = 'fixed';
        popup.style.top = `${rect.bottom + 6}px`;
        popup.style.left = `${rect.left + rect.width / 2 - 88}px`;

        // Ekran dışına taşarsa yukarıya al
        document.body.appendChild(popup);
        const popupRect = popup.getBoundingClientRect();
        if (popupRect.bottom > window.innerHeight) {
            popup.style.top = `${rect.top - popupRect.height - 6}px`;
        }
        if (popupRect.left < 8) {
            popup.style.left = '8px';
        }
        if (popupRect.right > window.innerWidth - 8) {
            popup.style.left = `${window.innerWidth - popupRect.width - 8}px`;
        }
    }

    function closeGenderPopup() {
        const p = document.getElementById('gender-popup');
        if (p) p.remove();
    }

    // ── Cinsiyet seçildi ──
    function selectGender(seatNo, gender) {
        closeGenderPopup();

        // Seçili yap
        selectedSeats.push({ seatNo, gender });

        const btn = document.getElementById(`seat-${seatNo}`);
        if (btn) {
            btn.className = btn.className
                .replace(/bg-white border-gray-300 hover:border-navy-600 hover:bg-navy-50/g, 'bg-emerald-500 border-emerald-600')
                .replace(/text-gray-700/g, 'text-white');
        }

        updateSelectedList();
    }

    // ── Seçilen koltuk listesini güncelle ──
    function updateSelectedList() {
        const listEl = document.querySelector(`[id^="selected-seats-list-"]`);
        const confirmBtn = document.querySelector(`[id^="confirm-seats-"]`);
        if (!listEl) return;

        if (selectedSeats.length === 0) {
            listEl.innerHTML = `<p class="text-xs text-gray-400 italic">Henüz koltuk seçilmedi</p>`;
            if (confirmBtn) confirmBtn.disabled = true;
            return;
        }

        if (confirmBtn) confirmBtn.disabled = false;

        listEl.innerHTML = selectedSeats.map(s => `
            <div class="flex items-center justify-between py-1.5 px-2 rounded-lg bg-emerald-50 mb-1.5">
                <div class="flex items-center gap-2">
                    <span class="w-6 h-6 rounded-md bg-emerald-500 text-white text-xs font-bold flex items-center justify-center">${s.seatNo}</span>
                    <span class="text-xs font-medium text-navy-700">${s.gender === 'male' ? '👨 Erkek' : '👩 Kadın'}</span>
                </div>
                <button onclick="SeatSelection.onSeatClick(event, ${s.seatNo})"
                    class="text-gray-400 hover:text-brand-600 transition cursor-pointer" title="Kaldır">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
            </div>
        `).join('');
    }

    // ── Onayla → Ödeme sayfasına yönlendir ──
    function confirm(tripId) {
        if (selectedSeats.length === 0) return;

        // Collect trip info from URL params and the card
        const urlParams = new URLSearchParams(window.location.search);
        const cardEl = document.getElementById(`trip-card-${tripId}`);
        const companyName = cardEl?.querySelector('.font-bold.text-navy-800.text-sm')?.textContent || '-';
        const time = cardEl?.querySelector('.text-2xl.font-extrabold.text-navy-800')?.textContent || '-';
        const priceText = cardEl?.querySelector('.text-brand-600.font-extrabold')?.textContent || '0';
        const price = parseFloat(priceText.replace(/[^\d.]/g, '')) || 0;

        const booking = {
            tripId,
            fromStopId: currentFromStopId,
            toStopId: currentToStopId,
            companyName,
            from: urlParams.get('from') || '-',
            to: urlParams.get('to') || '-',
            date: urlParams.get('date') || '-',
            time,
            busType: cardEl?.querySelector('[class*="bg-amber-50"], [class*="bg-gray-100"]')?.textContent?.trim() || '2+1',
            price,
            seats: [...selectedSeats]
        };

        sessionStorage.setItem('obilet_booking', JSON.stringify(booking));
        window.location.href = 'payment.html';
    }

    // Dışarıya tıklanırsa popup kapat
    document.addEventListener('click', (e) => {
        const popup = document.getElementById('gender-popup');
        if (popup && !popup.contains(e.target) && !e.target.classList.contains('seat-box')) {
            closeGenderPopup();
        }
    });

    return { toggle, close, onSeatClick, selectGender, closeGenderPopup, confirm };
})();

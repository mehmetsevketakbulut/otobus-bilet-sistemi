document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const from = urlParams.get('from');
    const to = urlParams.get('to');
    const date = urlParams.get('date');

    if (!from || !to || !date) {
        window.location.href = 'index.html';
        return;
    }

    // --- HEADER SETUP ---
    document.getElementById('headerFrom').textContent = from.toUpperCase();
    document.getElementById('headerTo').textContent = to.toUpperCase();
    const dateObj = new Date(date);
    document.getElementById('headerDate').textContent = dateObj.toLocaleDateString('tr-TR', {
        day: 'numeric', month: 'long', year: 'numeric'
    });
    document.title = `${from} → ${to} Seferleri | OtoBilet`;

    const resultsContainer = document.getElementById('resultsContainer');
    const loading = document.getElementById('loading');
    const noResults = document.getElementById('noResults');
    const tripCountEl = document.getElementById('tripCount');

    let allTrips = []; // master data

    // --- FETCH TRIPS ---
    try {
        const url = `/trips/search?fromCity=${encodeURIComponent(from)}&toCity=${encodeURIComponent(to)}&date=${date}`;
        const trips = await fetchApi(url, { method: 'GET' });
        allTrips = trips;
    } catch (error) {
        console.warn('API erişilemedi, demo veriler yükleniyor:', error);
        // Fallback demo data matching expected format
        allTrips = [
            { tripId: 101, companyName: "Kamil Koç", time: "08:00", price: 450, type: "2+1", departureTerminal: from + " Otogar", arrivalTerminal: to + " Otogar" },
            { tripId: 102, companyName: "Metro Turizm", time: "10:30", price: 380, type: "2+2", departureTerminal: from + " Otogar", arrivalTerminal: to + " Otogar" },
            { tripId: 103, companyName: "Pamukkale", time: "13:00", price: 520, type: "2+1", departureTerminal: from + " Otogar", arrivalTerminal: to + " Otogar" },
            { tripId: 104, companyName: "Süha Turizm", time: "16:00", price: 400, type: "2+2", departureTerminal: from + " Otogar", arrivalTerminal: to + " Terminal" },
            { tripId: 105, companyName: "Ulusoy", time: "19:30", price: 600, type: "2+1", departureTerminal: from + " Otogar", arrivalTerminal: to + " Otogar" },
            { tripId: 106, companyName: "Niğde Lüks", time: "22:00", price: 350, type: "2+2", departureTerminal: from + " Otogar", arrivalTerminal: to + " Otogar" },
            { tripId: 107, companyName: "FlixBus", time: "06:30", price: 490, type: "2+1", departureTerminal: from + " Otogar", arrivalTerminal: to + " Terminal" }
        ];
    }

    loading.style.display = 'none';

    if (allTrips.length === 0) {
        noResults.classList.remove('hidden');
        return;
    }

    // --- RENDER FUNCTION ---
    function renderTrips(trips) {
        resultsContainer.innerHTML = '';
        tripCountEl.textContent = trips.length;

        if (trips.length === 0) {
            noResults.classList.remove('hidden');
            return;
        }
        noResults.classList.add('hidden');

        trips.forEach((trip, index) => {
            const displayTime = trip.time || new Date(trip.departureTime).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
            const busType = trip.type || '2+1';
            const isComfort = busType === '2+1';
            const initials = trip.companyName.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();

            const card = document.createElement('div');
            card.className = 'trip-card animate-card bg-white rounded-2xl shadow-sm border border-gray-100 p-5 sm:p-6';
            card.style.animationDelay = `${index * 0.08}s`;
            card.id = `trip-card-${trip.tripId}`;

            card.innerHTML = `
                <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4">
                    <!-- Company -->
                    <div class="flex items-center gap-3 sm:w-48 flex-shrink-0">
                        <div class="w-12 h-12 rounded-xl bg-gradient-to-br ${isComfort ? 'from-navy-600 to-navy-800' : 'from-gray-600 to-gray-800'} flex items-center justify-center shadow-md">
                            <span class="text-white font-bold text-sm">${initials}</span>
                        </div>
                        <div>
                            <p class="font-bold text-navy-800 text-sm">${trip.companyName}</p>
                            <span class="inline-block mt-1 text-xs font-semibold px-2 py-0.5 rounded-md ${isComfort ? 'bg-amber-50 text-amber-700' : 'bg-gray-100 text-gray-600'}">${busType} ${isComfort ? '✨' : ''}</span>
                        </div>
                    </div>

                    <!-- Time & Route -->
                    <div class="flex-1 flex items-center gap-4 sm:justify-center">
                        <div class="text-center">
                            <p class="text-2xl font-extrabold text-navy-800">${displayTime}</p>
                            <p class="text-xs text-gray-400 mt-0.5">${trip.departureTerminal || ''}</p>
                        </div>
                        <div class="flex flex-col items-center gap-1 px-4">
                            <div class="flex items-center gap-1">
                                <div class="w-2 h-2 rounded-full bg-navy-600"></div>
                                <div class="w-16 sm:w-24 h-0.5 bg-gradient-to-r from-navy-600 to-brand-500 rounded"></div>
                                <div class="w-2 h-2 rounded-full bg-brand-500"></div>
                            </div>
                            <span class="text-[10px] text-gray-400 font-medium">Direkt</span>
                        </div>
                        <div class="text-center">
                            <p class="text-xs text-gray-400">${trip.arrivalTerminal || ''}</p>
                        </div>
                    </div>

                    <!-- Price & Action -->
                    <div class="flex items-center gap-4 sm:flex-col sm:items-end sm:w-44 flex-shrink-0 w-full sm:w-auto justify-between sm:justify-start">
                        <div class="text-right">
                            <p class="text-2xl font-extrabold text-brand-600">${Number(trip.price).toFixed(0)} <span class="text-base font-semibold">TL</span></p>
                            <p class="text-[10px] text-gray-400">kişi başı</p>
                        </div>
                        <button data-trip-id="${trip.tripId}" data-bus-type="${busType}" data-from-stop-id="${trip.fromStopId}" data-to-stop-id="${trip.toStopId}" class="select-seat-btn seat-btn bg-gradient-to-r from-brand-600 to-brand-700 hover:from-brand-700 hover:to-brand-800 text-white font-bold text-sm px-6 py-3 rounded-xl shadow-lg cursor-pointer whitespace-nowrap">
                            Koltuk Seç
                        </button>
                    </div>
                </div>
            `;
            resultsContainer.appendChild(card);
        });

        // Wire seat selection buttons
        resultsContainer.querySelectorAll('.select-seat-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const tripId = parseInt(btn.dataset.tripId);
                const busType = btn.dataset.busType;
                const fromStopId = parseInt(btn.dataset.fromStopId);
                const toStopId = parseInt(btn.dataset.toStopId);
                const cardEl = document.getElementById(`trip-card-${tripId}`);
                SeatSelection.toggle(tripId, busType, cardEl, fromStopId, toStopId);
            });
        });
    }

    // --- SORT & FILTER ---
    let currentSort = 'price-asc';
    let currentTypeFilter = 'all';

    function getFiltered() {
        let filtered = [...allTrips];
        if (currentTypeFilter !== 'all') {
            filtered = filtered.filter(t => (t.type || '2+1') === currentTypeFilter);
        }
        filtered.sort((a, b) => {
            if (currentSort === 'price-asc') return a.price - b.price;
            if (currentSort === 'price-desc') return b.price - a.price;
            const timeA = (a.time || '00:00').replace(':', '');
            const timeB = (b.time || '00:00').replace(':', '');
            if (currentSort === 'time-asc') return timeA - timeB;
            if (currentSort === 'time-desc') return timeB - timeA;
            return 0;
        });
        return filtered;
    }

    // Sort buttons
    document.querySelectorAll('[data-sort]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('[data-sort]').forEach(b => {
                b.classList.remove('active');
                b.classList.add('text-navy-700', 'bg-white');
            });
            btn.classList.add('active');
            btn.classList.remove('text-navy-700', 'bg-white');
            currentSort = btn.dataset.sort;
            renderTrips(getFiltered());
        });
    });

    // Bus type filter
    document.querySelectorAll('.bus-type-filter').forEach(cb => {
        cb.addEventListener('change', () => {
            if (cb.value === 'all') {
                document.querySelectorAll('.bus-type-filter').forEach(c => { if(c.value !== 'all') c.checked = false; });
                currentTypeFilter = 'all';
            } else {
                document.querySelector('.bus-type-filter[value="all"]').checked = false;
                const checked = [...document.querySelectorAll('.bus-type-filter:checked')].map(c => c.value).filter(v => v !== 'all');
                if (checked.length === 0) {
                    document.querySelector('.bus-type-filter[value="all"]').checked = true;
                    currentTypeFilter = 'all';
                } else if (checked.length === 1) {
                    currentTypeFilter = checked[0];
                } else {
                    currentTypeFilter = 'all';
                }
            }
            renderTrips(getFiltered());
        });
    });

    // Mobile menu
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', () => {
            document.getElementById('mobileMenu').classList.toggle('hidden');
        });
    }

    // Initial render
    renderTrips(getFiltered());
});

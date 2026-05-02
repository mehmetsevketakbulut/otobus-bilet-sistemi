document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const from = urlParams.get('from');
    const to = urlParams.get('to');
    const date = urlParams.get('date');

    if (!from || !to || !date) {
        window.location.href = 'index.html';
        return;
    }

    document.getElementById('headerFrom').textContent = from.toUpperCase();
    document.getElementById('headerTo').textContent = to.toUpperCase();
    
    const dateObj = new Date(date);
    document.getElementById('headerDate').textContent = dateObj.toLocaleDateString('tr-TR', {
        day: 'numeric', month: 'long', year: 'numeric'
    });

    const resultsContainer = document.getElementById('resultsContainer');
    const loading = document.getElementById('loading');
    const noResults = document.getElementById('noResults');

    try {
        // Obilet search API request
        const url = `/trips/search?fromCity=${encodeURIComponent(from)}&toCity=${encodeURIComponent(to)}&date=${date}`;
        
        // Use fetchApi wrapper
        const trips = await fetchApi(url, { method: 'GET' });

        loading.style.display = 'none';

        if (trips.length === 0) {
            noResults.style.display = 'block';
            return;
        }

        trips.forEach(trip => {
            const departureTime = new Date(trip.departureTime).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
            
            const card = document.createElement('div');
            card.className = 'ticket-card';
            
            card.innerHTML = `
                <div style="flex: 1;">
                    <div class="ticket-company">${trip.companyName}</div>
                    <div class="ticket-route" style="margin-top: 0.5rem;">
                        <span>${trip.departureTerminal}</span>
                        <span>➞</span>
                        <span>${trip.arrivalTerminal}</span>
                    </div>
                </div>
                
                <div style="text-align: center; flex: 1;">
                    <div style="color: var(--text-muted); font-size: 0.8rem; margin-bottom: 0.3rem;">Kalkış Saati</div>
                    <div class="ticket-time">${departureTime}</div>
                </div>

                <div style="text-align: right; flex: 1;">
                    <div class="ticket-price">${trip.price.toFixed(2)} TL</div>
                    <button class="btn btn-primary" style="margin-top: 0.5rem;" onclick="alert('Koltuk seçimi yakında eklenecek!')">Koltuk Seç</button>
                </div>
            `;
            
            resultsContainer.appendChild(card);
        });

    } catch (error) {
        loading.style.display = 'none';
        noResults.textContent = 'Arama yapılırken bir hata oluştu: ' + error.message;
        noResults.style.display = 'block';
    }
});

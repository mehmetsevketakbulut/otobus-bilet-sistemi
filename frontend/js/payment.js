document.addEventListener('DOMContentLoaded', () => {
    // ── Retrieve booking data from sessionStorage ──
    const bookingRaw = sessionStorage.getItem('obilet_booking');
    if (!bookingRaw) {
        alert('Rezervasyon bilgisi bulunamadı. Ana sayfaya yönlendiriliyorsunuz.');
        window.location.href = 'index.html';
        return;
    }

    const booking = JSON.parse(bookingRaw);
    // booking = { tripId, companyName, from, to, date, time, busType, price, seats: [{seatNo, gender}] }

    const seats = booking.seats || [];
    const pricePerSeat = booking.price || 0;
    const seatCount = seats.length;
    const serviceFee = seatCount * 9.90;
    const subtotal = seatCount * pricePerSeat;
    const total = subtotal + serviceFee;

    // ── Fill Summary Panel ──
    setText('summaryFrom', (booking.from || '').toUpperCase());
    setText('summaryTo', (booking.to || '').toUpperCase());
    setText('summaryCompany', booking.companyName || '-');
    setText('summaryTime', booking.time || '-');

    const dateEl = document.getElementById('summaryDate');
    if (dateEl && booking.date) {
        const d = new Date(booking.date);
        dateEl.innerHTML = `<svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>` +
            d.toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric' });
    }

    // Seat list in summary
    const summarySeatsEl = document.getElementById('summarySeats');
    seats.forEach(s => {
        const genderLabel = s.gender === 'male' ? 'Erkek' : 'Kadın';
        const genderIcon = s.gender === 'male' ? '👨' : '👩';
        const genderBg = s.gender === 'male' ? 'bg-blue-50 text-blue-700' : 'bg-pink-50 text-pink-700';
        summarySeatsEl.innerHTML += `
            <div class="flex items-center justify-between py-2 px-3 rounded-lg bg-gray-50">
                <div class="flex items-center gap-2">
                    <span class="w-7 h-7 rounded-lg bg-navy-600 text-white text-xs font-bold flex items-center justify-center">${s.seatNo}</span>
                    <span class="text-xs font-medium ${genderBg} px-2 py-0.5 rounded-md">${genderIcon} ${genderLabel}</span>
                </div>
                <span class="text-xs font-bold text-navy-800">${pricePerSeat} TL</span>
            </div>`;
    });

    setText('summarySubtotal', `${subtotal.toFixed(2)} TL`);
    setText('summaryFee', `${serviceFee.toFixed(2)} TL`);
    setText('summaryTotal', `${total.toFixed(2)} TL`);

    // ── Generate Passenger Forms ──
    const passengerForms = document.getElementById('passengerForms');
    seats.forEach((s, i) => {
        const genderIcon = s.gender === 'male' ? '👨' : '👩';
        const genderLabel = s.gender === 'male' ? 'Erkek' : 'Kadın';

        const card = document.createElement('div');
        card.className = 'bg-white rounded-2xl shadow-sm border border-gray-100 p-6 animate-in';
        card.style.animationDelay = `${i * 0.1}s`;

        card.innerHTML = `
            <h2 class="text-base font-bold text-navy-800 mb-4 flex items-center gap-2">
                <div class="w-7 h-7 rounded-lg bg-brand-50 flex items-center justify-center">
                    <span class="text-sm">${genderIcon}</span>
                </div>
                ${i + 1}. Yolcu — Koltuk ${s.seatNo}
                <span class="text-xs font-medium text-gray-400 ml-auto">${genderLabel}</span>
            </h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div class="sm:col-span-2">
                    <label class="block text-xs font-semibold text-gray-600 mb-1">T.C. Kimlik No *</label>
                    <input type="text" name="tc_${i}" placeholder="11 haneli T.C. kimlik numarası" required maxlength="11"
                        class="input-field tc-input w-full px-4 py-3 border-2 border-gray-200 rounded-xl text-sm text-navy-800 outline-none font-mono tracking-wider">
                    <p class="tc-error text-xs text-brand-600 mt-1 hidden">Geçerli bir T.C. Kimlik numarası girin</p>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-gray-600 mb-1">Ad *</label>
                    <input type="text" name="firstName_${i}" placeholder="Adınız" required
                        class="input-field w-full px-4 py-3 border-2 border-gray-200 rounded-xl text-sm text-navy-800 outline-none">
                </div>
                <div>
                    <label class="block text-xs font-semibold text-gray-600 mb-1">Soyad *</label>
                    <input type="text" name="lastName_${i}" placeholder="Soyadınız" required
                        class="input-field w-full px-4 py-3 border-2 border-gray-200 rounded-xl text-sm text-navy-800 outline-none">
                </div>
            </div>
        `;
        passengerForms.appendChild(card);
    });

    // ── T.C. Kimlik No Validation (11 digits, Luhn-like Turkish TC check) ──
    document.querySelectorAll('.tc-input').forEach(input => {
        input.addEventListener('input', (e) => {
            e.target.value = e.target.value.replace(/\D/g, '').slice(0, 11);
        });
        input.addEventListener('blur', (e) => {
            const val = e.target.value;
            const errEl = e.target.parentElement.querySelector('.tc-error');
            if (val.length > 0 && !validateTC(val)) {
                e.target.classList.add('error');
                errEl?.classList.remove('hidden');
            } else {
                e.target.classList.remove('error');
                errEl?.classList.add('hidden');
            }
        });
    });

    // ── Credit Card Formatting ──
    const cardNumberInput = document.getElementById('cardNumber');
    const cardExpiryInput = document.getElementById('cardExpiry');
    const cardCvvInput = document.getElementById('cardCvv');
    const cardNameInput = document.getElementById('cardName');

    // Card number: format as "0000 0000 0000 0000"
    cardNumberInput.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '').slice(0, 16);
        val = val.replace(/(\d{4})(?=\d)/g, '$1 ');
        e.target.value = val;
        // Update preview
        const raw = val.replace(/\s/g, '');
        let display = '';
        for (let i = 0; i < 16; i += 4) {
            display += (raw.substring(i, i + 4) || '••••') + ' ';
        }
        setText('cardNumberPreview', display.trim());
        // Detect brand
        detectCardBrand(raw);
    });

    // Expiry: format as "MM/YY"
    cardExpiryInput.addEventListener('input', (e) => {
        let val = e.target.value.replace(/\D/g, '').slice(0, 4);
        if (val.length >= 3) val = val.slice(0, 2) + '/' + val.slice(2);
        e.target.value = val;
        setText('cardExpiryPreview', val || 'MM/YY');
    });

    // CVV: only digits
    cardCvvInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').slice(0, 3);
    });

    // Name preview
    cardNameInput.addEventListener('input', (e) => {
        setText('cardNamePreview', e.target.value.toUpperCase() || 'AD SOYAD');
    });

    // ── Phone formatting ──
    const phoneInput = document.getElementById('contactPhone');
    phoneInput.addEventListener('input', (e) => {
        e.target.value = e.target.value.replace(/\D/g, '').slice(0, 11);
    });

    // ── Form Submit ──
    document.getElementById('paymentForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        // Validate all
        if (!validateForm()) return;

        const payBtn = document.getElementById('payBtn');
        payBtn.disabled = true;
        payBtn.innerHTML = `<svg class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg> İşleniyor...`;

        // Build POST payload
        const passengers = seats.map((s, i) => ({
            seatNo: s.seatNo,
            gender: s.gender,
            tcNo: document.querySelector(`[name="tc_${i}"]`).value,
            firstName: document.querySelector(`[name="firstName_${i}"]`).value.trim(),
            lastName: document.querySelector(`[name="lastName_${i}"]`).value.trim()
        }));

        const payload = {
            tripId: booking.tripId,
            passengers,
            contact: {
                email: document.getElementById('contactEmail').value.trim(),
                phone: document.getElementById('contactPhone').value.trim()
            },
            payment: {
                cardName: cardNameInput.value.trim(),
                cardNumber: cardNumberInput.value.replace(/\s/g, ''),
                expiry: cardExpiryInput.value,
                cvv: cardCvvInput.value
            },
            totalAmount: total
        };

        console.log('💳 Ödeme payload hazır:', payload);

        // Helper to navigate to confirmation
        function goToConfirmation(pnr, passengers) {
            const confirmationData = {
                pnr,
                from: booking.from,
                to: booking.to,
                date: booking.date,
                time: booking.time,
                companyName: booking.companyName,
                passengers,
                totalAmount: total
            };
            sessionStorage.setItem('obilet_confirmation', JSON.stringify(confirmationData));
            sessionStorage.removeItem('obilet_booking');
            window.location.href = 'confirmation.html';
        }

        try {
            // POST request to backend
            const result = await fetchApi('/payments', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            goToConfirmation(result.pnr || 'OB-' + Date.now(), passengers);
        } catch (err) {
            // Demo mode: simulate success
            console.warn('API erişilemedi, demo mod:', err);
            await sleep(1500);
            const demoPnr = 'OB-' + Math.random().toString(36).substr(2, 6).toUpperCase();
            goToConfirmation(demoPnr, passengers);
        }
    });

    // ── Helpers ──
    function setText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }

    function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }

    function validateTC(tc) {
        if (tc.length !== 11) return false;
        if (tc[0] === '0') return false;
        const digits = tc.split('').map(Number);
        const sumOdd = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        const sumEven = digits[1] + digits[3] + digits[5] + digits[7];
        const check10 = (sumOdd * 7 - sumEven) % 10;
        if (check10 !== digits[9]) return false;
        const checkSum = digits.slice(0, 10).reduce((a, b) => a + b, 0) % 10;
        return checkSum === digits[10];
    }

    function detectCardBrand(num) {
        const brandEl = document.getElementById('cardBrand');
        if (!brandEl) return;
        if (num.startsWith('4')) brandEl.textContent = 'VISA';
        else if (/^5[1-5]/.test(num)) brandEl.textContent = 'MASTERCARD';
        else if (/^3[47]/.test(num)) brandEl.textContent = 'AMEX';
        else if (/^(36|38|30)/.test(num)) brandEl.textContent = 'DINERS';
        else brandEl.textContent = '';
    }

    function validateForm() {
        let valid = true;
        // Check TC numbers
        document.querySelectorAll('.tc-input').forEach(input => {
            if (!validateTC(input.value)) {
                input.classList.add('error');
                input.parentElement.querySelector('.tc-error')?.classList.remove('hidden');
                valid = false;
            }
        });
        // Check required fields
        document.querySelectorAll('#paymentForm [required]').forEach(f => {
            if (!f.value.trim()) {
                f.classList.add('error');
                valid = false;
            } else {
                f.classList.remove('error');
            }
        });
        // Email check
        const email = document.getElementById('contactEmail');
        if (email.value && !email.value.includes('@')) {
            email.classList.add('error');
            valid = false;
        }
        // Card number check (16 digits)
        if (cardNumberInput.value.replace(/\s/g, '').length !== 16) {
            cardNumberInput.classList.add('error');
            valid = false;
        }
        // CVV check
        if (cardCvvInput.value.length < 3) {
            cardCvvInput.classList.add('error');
            valid = false;
        }
        if (!valid) {
            // Scroll to first error
            const firstError = document.querySelector('.error');
            if (firstError) firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return valid;
    }
});

/* ═══════════════════════════════════
   FOREX CONVERTER — Frontend Logic
   ═══════════════════════════════════ */

// ── Config ──────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/currency-converter';

// Currency list with symbols
const CURRENCIES = [
  { code: 'USD', name: 'US Dollar',           symbol: '$'  },
  { code: 'EUR', name: 'Euro',                symbol: '€'  },
  { code: 'GBP', name: 'British Pound',       symbol: '£'  },
  { code: 'INR', name: 'Indian Rupee',        symbol: '₹'  },
  { code: 'JPY', name: 'Japanese Yen',        symbol: '¥'  },
  { code: 'AUD', name: 'Australian Dollar',   symbol: 'A$' },
  { code: 'CAD', name: 'Canadian Dollar',     symbol: 'C$' },
  { code: 'CHF', name: 'Swiss Franc',         symbol: 'Fr' },
  { code: 'CNY', name: 'Chinese Yuan',        symbol: '¥'  },
  { code: 'HKD', name: 'Hong Kong Dollar',   symbol: 'HK$'},
  { code: 'SGD', name: 'Singapore Dollar',   symbol: 'S$' },
  { code: 'KRW', name: 'South Korean Won',   symbol: '₩'  },
  { code: 'MXN', name: 'Mexican Peso',       symbol: '$'  },
  { code: 'BRL', name: 'Brazilian Real',     symbol: 'R$' },
  { code: 'ZAR', name: 'South African Rand', symbol: 'R'  },
  { code: 'AED', name: 'UAE Dirham',         symbol: 'د.إ'},
  { code: 'SAR', name: 'Saudi Riyal',        symbol: '﷼'  },
  { code: 'SEK', name: 'Swedish Krona',      symbol: 'kr' },
  { code: 'NOK', name: 'Norwegian Krone',    symbol: 'kr' },
  { code: 'DKK', name: 'Danish Krone',       symbol: 'kr' },
  { code: 'NZD', name: 'New Zealand Dollar', symbol: 'NZ$'},
  { code: 'RUB', name: 'Russian Ruble',      symbol: '₽'  },
  { code: 'TRY', name: 'Turkish Lira',       symbol: '₺'  },
  { code: 'THB', name: 'Thai Baht',          symbol: '฿'  },
  { code: 'MYR', name: 'Malaysian Ringgit',  symbol: 'RM' },
  { code: 'IDR', name: 'Indonesian Rupiah',  symbol: 'Rp' },
  { code: 'PHP', name: 'Philippine Peso',    symbol: '₱'  },
  { code: 'PKR', name: 'Pakistani Rupee',    symbol: '₨'  },
  { code: 'EGP', name: 'Egyptian Pound',     symbol: '£'  },
  { code: 'NGN', name: 'Nigerian Naira',     symbol: '₦'  },
];

// ── DOM References ───────────────────────────────────────
const $  = id => document.getElementById(id);
const amountInput    = $('amount');
const fromSelect     = $('fromCurrency');
const toSelect       = $('toCurrency');
const convertBtn     = $('convertBtn');
const spinner        = $('spinner');
const btnText        = convertBtn.querySelector('.btn-text');
const resultArea     = $('resultArea');
const resultLabel    = $('resultLabel');
const resultValue    = $('resultValue');
const resultRate     = $('resultRate');
const historyList    = $('historyList');
const swapBtn        = $('swapBtn');
const refreshBtn     = $('refreshHistory');
const fromSymbol     = $('fromSymbol');

// ── Init ─────────────────────────────────────────────────
function init() {
  populateSelects();
  loadHistory();

  convertBtn.addEventListener('click', handleConvert);
  swapBtn.addEventListener('click', handleSwap);
  refreshBtn.addEventListener('click', () => {
    refreshBtn.classList.add('spinning');
    loadHistory().finally(() => {
      setTimeout(() => refreshBtn.classList.remove('spinning'), 600);
    });
  });

  fromSelect.addEventListener('change', updateSymbol);
  amountInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') handleConvert();
  });
}

// ── Populate Currency Dropdowns ──────────────────────────
function populateSelects() {
  [fromSelect, toSelect].forEach((sel, i) => {
    CURRENCIES.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.code;
      opt.textContent = `${c.code} — ${c.name}`;
      sel.appendChild(opt);
    });
  });
  fromSelect.value = 'USD';
  toSelect.value   = 'INR';
  updateSymbol();
}

function updateSymbol() {
  const cur = CURRENCIES.find(c => c.code === fromSelect.value);
  fromSymbol.textContent = cur ? cur.symbol : '$';
}

// ── Swap Currencies ──────────────────────────────────────
function handleSwap() {
  const tmp = fromSelect.value;
  fromSelect.value = toSelect.value;
  toSelect.value   = tmp;
  updateSymbol();
  if (!resultArea.classList.contains('d-none')) {
    resultArea.classList.add('d-none');
  }
}

// ── Convert ──────────────────────────────────────────────
async function handleConvert() {
  const amount = parseFloat(amountInput.value);

  // Validate
  if (!amountInput.value.trim() || isNaN(amount) || amount <= 0) {
    showToast('Please enter a valid amount greater than 0.');
    amountInput.focus();
    return;
  }

  if (fromSelect.value === toSelect.value) {
    showToast('Please select different currencies to convert.');
    return;
  }

  const payload = {
    amount:       amount,
    fromCurrency: fromSelect.value,
    toCurrency:   toSelect.value
  };

  // Loading state
  setLoading(true);

  try {
    const res  = await fetch(`${API_BASE}/convert`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify(payload)
    });

    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || `Server error: ${res.status}`);
    }

    const data = await res.json();
    showResult(data, amount);
    await loadHistory();

  } catch (err) {
    console.error('Conversion error:', err);
    showToast(err.message || 'Conversion failed. Is the backend running?');
  } finally {
    setLoading(false);
  }
}

// ── Show Result ──────────────────────────────────────────
function showResult(data, amount) {
  const fmtAmount = formatNum(amount);
  const fmtResult = formatNum(data.result);
  const rate      = (data.result / amount).toFixed(6);

  resultLabel.textContent = `${fmtAmount} ${data.fromCurrency} equals`;
  resultValue.textContent = `${fmtResult} ${data.toCurrency}`;
  resultRate.textContent  = `1 ${data.fromCurrency} = ${rate} ${data.toCurrency}`;

  resultArea.classList.remove('d-none');
  // Trigger re-animation
  resultArea.style.animation = 'none';
  resultArea.offsetHeight; // reflow
  resultArea.style.animation = '';
}

// ── Load History ─────────────────────────────────────────
async function loadHistory() {
  try {
    const res  = await fetch(`${API_BASE}/history`);
    if (!res.ok) throw new Error(`History fetch failed: ${res.status}`);
    const data = await res.json();
    renderHistory(data);
  } catch (err) {
    console.error('History error:', err);
    // Silently fail — don't spam toasts on page load
  }
}

// ── Render History ───────────────────────────────────────
function renderHistory(items) {
  if (!items || items.length === 0) {
    historyList.innerHTML = `
      <div class="history-empty">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/><path d="M12 6v6l4 2"/></svg>
        <p>No conversions yet.<br/>Make your first one above.</p>
      </div>`;
    return;
  }

  historyList.innerHTML = items.map(item => `
    <div class="history-item">
      <div class="history-conversion">
        <span class="h-amount">${formatNum(item.amount)}</span>
        <span class="h-from">${item.fromCurrency}</span>
        <span class="h-arrow">→</span>
        <span class="h-to">${item.toCurrency}</span>
        <span class="h-eq">=</span>
        <span class="h-result">${formatNum(item.result)}</span>
        <span class="h-to">${item.toCurrency}</span>
      </div>
      <div class="history-time">${formatTime(item.timestamp)}</div>
    </div>
  `).join('');
}

// ── Helpers ──────────────────────────────────────────────
function setLoading(on) {
  convertBtn.disabled = on;
  spinner.classList.toggle('d-none', !on);
  btnText.textContent = on ? 'Converting…' : 'Convert';
}

function formatNum(n) {
  if (n === undefined || n === null) return '—';
  const num = parseFloat(n);
  if (isNaN(num)) return '—';
  // Use up to 6 sig figs, strip trailing zeros
  if (num >= 1000)      return num.toLocaleString('en-US', { maximumFractionDigits: 2 });
  if (num >= 1)         return num.toLocaleString('en-US', { maximumFractionDigits: 4 });
  return num.toLocaleString('en-US', { maximumFractionDigits: 6 });
}

function formatTime(ts) {
  if (!ts) return '';
  const d = new Date(ts);
  if (isNaN(d)) return ts;
  const now = new Date();
  const diff = Math.floor((now - d) / 1000);
  if (diff < 60)  return 'just now';
  if (diff < 120) return '1 min ago';
  if (diff < 3600) return `${Math.floor(diff/60)} min ago`;
  return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

function showToast(msg) {
  // Remove existing
  const old = document.querySelector('.toast-container');
  if (old) old.remove();

  const wrap = document.createElement('div');
  wrap.className = 'toast-container';
  wrap.innerHTML = `<div class="toast-msg">${msg}</div>`;
  document.body.appendChild(wrap);

  setTimeout(() => wrap.remove(), 3500);
}

// ── Boot ─────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', init);

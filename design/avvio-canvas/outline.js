(async (file, onlyDark = true) => {
  const call = (m, b) => fetch('/design/anthropic.omelette.api.v1alpha.OmeletteService/' + m, {
    method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(b),
  }).then(r => r.json());
  const dec = b => new TextDecoder().decode(Uint8Array.from(atob(b), c => c.charCodeAt(0)));
  const html = dec((await call('GetFile', { projectId: 'f5d63256-9ace-489c-963c-a0a6622aaf96', path: file })).content);
  const d = new DOMParser().parseFromString(html, 'text/html');
  const T = {
    '#000000': 'bg', '#161817': 'card', '#232524': 'card2', '#3A3D3B': 'line', '#FFFFFF': 'text', '#8C918E': 'mute',
    '#5F6461': 'dim', '#AEFF00': 'LIME', '#FF7A7A': 'err', '#2A1416': 'errBg', '#FFC83D': 'warn', '#262009': 'warnBg',
    '#18230A': 'okBg', '#121413': 'sheet', '#FFF': 'text', '#000': 'bg',
  };
  const tok = c => T[c.toUpperCase()] || c;
  const outline = el => {
    const out = [];
    const walk = (n, dep) => {
      if (n.nodeType !== 1 || n.tagName === 'svg') return;
      const st = n.getAttribute('style') || '';
      const g = re => { const m = st.match(re); return m ? m[1] : null; };
      const bits = [];
      const bg = g(/background:(#[0-9A-Fa-f]{3,6})/); if (bg) bits.push('bg=' + tok(bg));
      const sh = g(/inset 0 0 0 ([\d.]+px [^;,"]+)/); if (sh) bits.push('outline=' + sh.replace(/#[0-9A-Fa-f]{6}/, tok));
      const r = g(/border-radius:([^;]+)/); if (r && bg) bits.push('r=' + r);
      const h = g(/(?:^|;)height:(\d+px)/); if (h) bits.push('h=' + h);
      const mh = g(/min-height:(\d+px)/); if (mh) bits.push('minh=' + mh);
      const w = g(/(?:^|;)width:(\d+px)/); if (w && bg) bits.push('w=' + w);
      const pad = g(/padding:([^;]+)/); if (pad && bg) bits.push('pad=' + pad);
      const gap = g(/gap:(\d+px)/); if (gap && /flex/.test(st)) bits.push('gap=' + gap);
      const own = [...n.childNodes].filter(c => c.nodeType === 3).map(c => c.textContent.trim()).join(' ').trim();
      const fs = g(/font-size:([\d.]+)px/), fw = g(/font-weight:(\d+)/), col = g(/(?:^|;)color:(#[0-9A-Fa-f]{3,6})/);
      if (fs) bits.push((/Archivo/.test(st) ? 'AB ' : '') + fs + '/' + (fw || '') + (/uppercase/.test(st) ? ' UP' : '') + (col ? ' ' + tok(col) : ''));
      if ([...n.children].some(c => c.tagName === 'svg')) bits.push('icon');
      if (own || (bits.length && (bg || sh))) out.push(' '.repeat(dep) + (own ? '"' + own + '" ' : '') + bits.join(' '));
      [...n.children].forEach(c => walk(c, dep + ((own || bg || sh) ? 1 : 0)));
    };
    walk(el.children[1], 0);
    return out.join('\n');
  };
  return [...d.querySelectorAll('[data-screen-label]')]
    .filter(e => !onlyDark || e.dataset.screenLabel.endsWith(' Dark'))
    .map(e => '### ' + e.dataset.screenLabel + '\n' + outline(e))
    .join('\n\n');
})

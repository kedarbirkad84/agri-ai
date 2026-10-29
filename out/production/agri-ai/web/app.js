async function call(path, form) {
  const qs = new URLSearchParams(new FormData(form)).toString();
  const r = await fetch(`/api/${path}?${qs}`);
  if (!r.ok) throw new Error((await r.json()).error || "Server error");
  return r.json();
}
const esc = s => String(s).replace(/[&<>"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

function wire(formId, outId, path, render) {
  document.getElementById(formId).addEventListener("submit", async e => {
    e.preventDefault();
    const out = document.getElementById(outId);
    out.innerHTML = '<div class="res">Analysing…</div>';
    try { out.innerHTML = render(await call(path, e.target)); }
    catch (err) { out.innerHTML = `<div class="res">⚠️ ${esc(err.message)} – is the Java server and database running?</div>`; }
  });
}

wire("cropForm", "cropOut", "crop", rows => rows.length
  ? rows.map(c => `<div class="res"><h4>🌾 ${esc(c.name)}</h4><p><b>Season:</b> ${esc(c.season)}</p><p>${esc(c.tips)}</p></div>`).join("")
  : '<div class="res">No exact match. Try adjusting pH, rainfall or temperature.</div>');

wire("soilForm", "soilOut", "soil", d =>
  `<div class="res"><h4>Soil advice</h4>${d.advice.map(a => `<p>• ${esc(a)}</p>`).join("")}</div>
   <div class="res"><h4>Suitable crops for this soil</h4>${d.crops.map(c => `<span class="tag">${esc(c)}</span>`).join("")}</div>`);

wire("disForm", "disOut", "disease", rows => rows.length
  ? rows.map(d => `<div class="res"><h4>🍃 ${esc(d.disease)}</h4><p><b>Cause:</b> ${esc(d.cause)}</p><p><b>Treatment:</b> ${esc(d.treatment)}</p></div>`).join("")
  : '<div class="res">No matching disease found. Try another symptom or consult a local expert.</div>');

document.getElementById("photo").addEventListener("change", e => {
  const f = e.target.files[0], img = document.getElementById("preview");
  if (f) { img.src = URL.createObjectURL(f); img.hidden = false; }
});

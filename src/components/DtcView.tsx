import React from 'react';
import { useOBDContext } from '../OBDContext';
import { AlertTriangle, CheckCircle2, RotateCcw, Trash2, Cpu } from 'lucide-react';

export const DtcView: React.FC = () => {
  const { dtcs, settings, clearDtcs, refreshDtcs, isConnected } = useOBDContext();

  return (
    <div className="flex flex-col gap-4 w-full">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-slate-800 pb-3 gap-3">
        <div>
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-red-500" />
            <h2 className="text-sm font-mono font-bold text-red-400 uppercase tracking-wider">
              DIAGNOSTIC TROUBLE CODES (DTC FAULTS)
            </h2>
          </div>
          <div className="flex items-center gap-2 text-xs text-slate-400 mt-1 font-mono">
            <span className="text-cyan-400 font-semibold">{settings.profileName}</span>
            <span>·</span>
            <span className="flex items-center gap-1 text-slate-300">
              <Cpu className="w-3 h-3 text-cyan-400" />
              <span>{settings.specs.cylinderCount} Silinder ({settings.specs.engineDisplacementCc} cc)</span>
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={refreshDtcs}
            className="px-3 py-1.5 rounded-lg text-xs font-mono font-bold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors flex items-center gap-1.5 cursor-pointer"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>SCAN ULANG ECU</span>
          </button>

          <button
            type="button"
            onClick={clearDtcs}
            disabled={dtcs.length === 0}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-bold flex items-center gap-1.5 transition-colors cursor-pointer ${
              dtcs.length > 0
                ? 'bg-red-600/20 text-red-300 border border-red-500/40 hover:bg-red-600/30'
                : 'bg-slate-900 text-slate-600 border border-slate-800 cursor-not-allowed opacity-50'
            }`}
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>HAPUS KODE (CLEAR DTCs)</span>
          </button>
        </div>
      </div>

      {/* Notice about single/multi cylinder diagnostics */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl px-4 py-2 text-xs font-mono text-slate-400 flex items-center justify-between">
        <span>
          Filter Diagnostik: Memindai kerusakan sistem spesifik motor <strong>{settings.specs.cylinderCount} Silinder</strong>.
        </span>
        <span className="text-[10px] text-cyan-400 font-bold">
          {dtcs.length} KODE TERDETEKSI
        </span>
      </div>

      {dtcs.length === 0 ? (
        <div className="bg-[#11141A] border border-[#1E2530] rounded-2xl p-10 flex flex-col items-center justify-center text-center gap-3 shadow-xl">
          <div className="w-12 h-12 rounded-full bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <h3 className="text-base font-mono font-bold text-slate-200">
            TIDAK ADA KODE KERUSAKAN (NO FAULT CODES)
          </h3>
          <p className="text-xs text-slate-400 max-w-md">
            Semua sistem sensor ECU motor {settings.profileName} ({settings.specs.cylinderCount} Silinder) beroperasi normal. Tidak ada error aktif atau pending yang terdeteksi.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {dtcs.map((dtc) => (
            <div
              key={dtc.code}
              className="bg-[#11141A] border border-red-900/40 hover:border-red-500/50 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 shadow-lg transition-all"
            >
              <div className="flex items-start gap-3">
                <span className="px-2.5 py-1 rounded bg-red-950/80 border border-red-800 text-red-300 font-mono text-sm font-extrabold shrink-0">
                  {dtc.code}
                </span>
                <div>
                  <h4 className="text-sm font-bold text-slate-100">
                    {dtc.description}
                  </h4>
                  <div className="flex items-center gap-2 text-xs text-slate-400 mt-1 font-mono">
                    <span>Kategori: {dtc.category}</span>
                    <span aria-hidden="true">·</span>
                    <span className="text-red-400 font-bold uppercase">{dtc.status}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

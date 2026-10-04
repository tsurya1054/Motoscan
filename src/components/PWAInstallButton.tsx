import React, { useState } from 'react';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { Smartphone, Download, CheckCircle2, X } from 'lucide-react';

export const PWAInstallButton: React.FC<{ variant?: 'header' | 'banner' }> = ({
  variant = 'header',
}) => {
  const { isInstallable, isInstalled, isIOS, install } = usePWAInstall();
  const [showGuideModal, setShowGuideModal] = useState(false);

  // If already running standalone on the device
  if (isInstalled) {
    return (
      <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-emerald-950/80 border border-emerald-500/40 text-[11px] font-mono text-emerald-400 font-bold">
        <CheckCircle2 className="w-3.5 h-3.5" />
        <span>TERPASANG</span>
      </div>
    );
  }

  const handleInstallClick = async () => {
    if (isInstallable) {
      const success = await install();
      if (!success) {
        setShowGuideModal(true);
      }
    } else {
      setShowGuideModal(true);
    }
  };

  return (
    <>
      <button
        type="button"
        onClick={handleInstallClick}
        className={`flex items-center gap-1.5 rounded-lg font-mono font-bold transition-all cursor-pointer shadow-lg active:scale-95 ${
          variant === 'banner'
            ? 'px-4 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 text-xs shadow-cyan-500/25'
            : 'px-3 py-1.5 bg-gradient-to-r from-cyan-400 to-cyan-500 hover:from-cyan-300 hover:to-cyan-400 text-slate-950 text-xs shadow-cyan-400/20'
        }`}
        title="Pasang aplikasi langsung ke perangkat Anda"
      >
        <Download className="w-3.5 h-3.5" />
        <span>INSTAL</span>
      </button>

      {/* Guided Modal for Android Chrome & iOS Safari */}
      {showGuideModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#151922] border border-[#2E394A] rounded-2xl max-w-md w-full p-5 shadow-2xl space-y-4 font-mono text-xs">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div className="flex items-center gap-2 text-cyan-400">
                <Smartphone className="w-4 h-4" />
                <h3 className="text-sm font-bold uppercase tracking-wider">
                  INSTAL APLIKASI MOTOSCAN ADV
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setShowGuideModal(false)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-slate-300 text-xs leading-relaxed">
              Anda dapat memasang aplikasi ini langsung ke layar utama (*Home Screen*) perangkat Anda:
            </p>

            {isIOS ? (
              <div className="bg-[#1B212D] border border-slate-700 rounded-xl p-3.5 space-y-2 text-slate-300">
                <div className="font-bold text-white text-[11px]">
                  📱 Panduan untuk iOS (Safari):
                </div>
                <ol className="list-decimal list-inside space-y-1.5 text-[11px] text-slate-400">
                  <li>Buka aplikasi ini di browser <strong>Safari</strong>.</li>
                  <li>Ketuk tombol <strong>Share / Bagikan</strong> di bilah navigasi Safari.</li>
                  <li>Pilih <strong>"Tambahkan ke Layar Utama" (Add to Home Screen)</strong>.</li>
                  <li>Ketuk <strong>Tambah (Add)</strong>.</li>
                </ol>
              </div>
            ) : (
              <div className="bg-[#1B212D] border border-slate-700 rounded-xl p-3.5 space-y-2 text-slate-300">
                <div className="font-bold text-white text-[11px]">
                  📱 Panduan untuk Android (Google Chrome):
                </div>
                <ol className="list-decimal list-inside space-y-1.5 text-[11px] text-slate-400">
                  <li>Buka aplikasi ini di browser <strong>Google Chrome</strong>.</li>
                  <li>Ketuk ikon <strong>titik tiga (⋮)</strong> di pojok kanan atas browser.</li>
                  <li>Pilih <strong>"Instal Aplikasi"</strong> atau <strong>"Tambahkan ke Layar Utama"</strong>.</li>
                  <li>Ketuk <strong>Instal</strong>.</li>
                </ol>
              </div>
            )}

            <div className="pt-2">
              <button
                type="button"
                onClick={() => setShowGuideModal(false)}
                className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold rounded-lg cursor-pointer"
              >
                MENGERTI & TUTUP
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

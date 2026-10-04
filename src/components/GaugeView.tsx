import React, { useState } from 'react';
import { useOBDContext } from '../OBDContext';
import { DialGauge } from './DialGauge';
import { PID } from '../types';
import {
  Radio,
  Disc,
  Plus,
  ArrowLeft,
  ArrowRight,
  Trash2,
  Settings as SettingsIcon,
  ZoomIn,
  ZoomOut,
  X,
  AlertTriangle,
  RotateCcw,
  PowerOff,
  Activity,
} from 'lucide-react';

export interface WidgetConfig {
  id: string;
  pidId: string;
  displayType: 'dial' | 'card'; // Every widget can toggle between circular dial or card!
  size: 'small' | 'medium' | 'large';
  scale: number; // 0.6 to 1.6
}

const DEFAULT_WIDGETS: WidgetConfig[] = [
  { id: 'w_rpm', pidId: 'rpm', displayType: 'dial', size: 'large', scale: 1.0 },
  { id: 'w_speed', pidId: 'speed', displayType: 'dial', size: 'large', scale: 1.0 },
  { id: 'w_coolant', pidId: 'coolant', displayType: 'card', size: 'medium', scale: 1.0 },
  { id: 'w_battery', pidId: 'battery', displayType: 'card', size: 'medium', scale: 1.0 },
  { id: 'w_throttle', pidId: 'throttle', displayType: 'card', size: 'medium', scale: 1.0 },
  { id: 'w_load', pidId: 'load', displayType: 'card', size: 'medium', scale: 1.0 },
];

export const GaugeView: React.FC = () => {
  const {
    pids,
    isConnected,
    isRecording,
    graphData,
    settings,
    setIsConnected,
    toggleRecording,
    updatePIDConfig,
  } = useOBDContext();

  const [widgets, setWidgets] = useState<WidgetConfig[]>(() => {
    try {
      const saved = localStorage.getItem('motoscan_unified_widgets');
      if (saved) return JSON.parse(saved);
    } catch {
      // Fallback
    }
    return DEFAULT_WIDGETS;
  });

  const [editLayoutMode, setEditLayoutMode] = useState<boolean>(false);
  const [showAddWidgetDialog, setShowAddWidgetDialog] = useState<boolean>(false);
  const [activeSlotToChange, setActiveSlotToChange] = useState<string | null>(null);
  const [pidToConfigure, setPidToConfigure] = useState<PID | null>(null);

  const saveWidgets = (newWidgets: WidgetConfig[]) => {
    setWidgets(newWidgets);
    localStorage.setItem('motoscan_unified_widgets', JSON.stringify(newWidgets));
  };

  const handleUpdateWidget = (widgetId: string, partial: Partial<WidgetConfig>) => {
    const updated = widgets.map((w) => (w.id === widgetId ? { ...w, ...partial } : w));
    saveWidgets(updated);
  };

  const handleDeleteWidget = (widgetId: string) => {
    const updated = widgets.filter((w) => w.id !== widgetId);
    saveWidgets(updated);
  };

  const handleMoveWidget = (fromIndex: number, toIndex: number) => {
    if (toIndex < 0 || toIndex >= widgets.length) return;
    const copy = [...widgets];
    const item = copy[fromIndex];
    copy[fromIndex] = copy[toIndex];
    copy[toIndex] = item;
    saveWidgets(copy);
  };

  const handleResetLayout = () => {
    saveWidgets(DEFAULT_WIDGETS);
  };

  const getSparklinePoints = (pidId: string) => {
    if (!isConnected) return [];
    return graphData.map((d) => (d as any)[pidId] ?? 0).slice(-20);
  };

  return (
    <div className="flex flex-col gap-4 w-full">
      {/* Top Header Status & Vehicle Specs Bar */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl px-4 py-3 flex flex-wrap items-center justify-between gap-3 shadow-lg">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <span
              className={`w-3 h-3 rounded-full transition-colors ${
                isConnected
                  ? 'bg-[#00E5FF] shadow-[0_0_10px_#00E5FF]'
                  : 'bg-red-500'
              }`}
            />
            <span className="font-mono text-xs font-bold text-slate-200">
              {isConnected ? 'OBD CONNECTED (ELM327)' : 'DISCONNECTED (STANDBY)'}
            </span>
          </div>
          <span className="text-slate-600">|</span>
          <div className="flex items-center gap-2 text-xs font-mono text-cyan-400 font-semibold">
            <span>{settings.profileName}</span>
            <span className="px-2 py-0.5 rounded bg-cyan-950/80 border border-cyan-800/60 text-[10px] text-cyan-300 font-bold">
              {settings.specs.engineDisplacementCc} cc · {settings.specs.cylinderCount} Silinder
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Record Button */}
          <button
            type="button"
            onClick={toggleRecording}
            disabled={!isConnected}
            className={`px-3 py-1.5 rounded-lg text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer ${
              !isConnected
                ? 'bg-slate-900 text-slate-600 border border-slate-800 cursor-not-allowed opacity-50'
                : isRecording
                ? 'bg-red-600 text-white animate-pulse shadow-md shadow-red-600/40'
                : 'bg-[#181E27] text-slate-300 hover:text-white border border-slate-700'
            }`}
          >
            <Disc className="w-3.5 h-3.5" />
            <span>{isRecording ? 'MEREKAM LOG...' : 'REKAM LOG'}</span>
          </button>

          {/* Connect / Disconnect */}
          <button
            type="button"
            onClick={() => setIsConnected(!isConnected)}
            className={`px-3.5 py-1.5 rounded-lg text-xs font-mono font-bold border transition-colors flex items-center gap-1.5 cursor-pointer shadow-md ${
              isConnected
                ? 'bg-red-950/80 text-red-400 hover:bg-red-900/80 border-red-800'
                : 'bg-[#00E5FF] text-slate-950 hover:bg-[#33ebff] border-[#00E5FF] font-extrabold shadow-[0_0_12px_rgba(0,229,255,0.3)]'
            }`}
          >
            <Radio className="w-3.5 h-3.5" />
            <span>{isConnected ? 'DISCONNECT' : 'CONNECT ELM327'}</span>
          </button>
        </div>
      </div>

      {/* Disconnected Standby Notice Banner */}
      {!isConnected && (
        <div className="bg-[#11141A] border border-amber-500/30 rounded-xl px-4 py-2.5 flex items-center justify-between text-xs font-mono text-amber-300 shadow-md">
          <div className="flex items-center gap-2.5">
            <PowerOff className="w-4 h-4 text-amber-400 shrink-0" />
            <span>
              <strong>Perangkat OBD2 Belum Terhubung:</strong> Semua nilai sensor dalam keadaan reset bersih (0). Klik tombol <strong>CONNECT ELM327</strong> di atas untuk mulai membaca telemetri mesin motor secara real-time.
            </span>
          </div>
        </div>
      )}

      {/* EDIT LAYOUT CONTROLS ROW */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl px-4 py-2.5 flex flex-wrap items-center justify-between gap-3 shadow-md">
        <div className="flex items-center gap-2.5">
          <span className="text-xs font-mono font-bold text-slate-300 uppercase">
            EDIT LAYOUT
          </span>
          <button
            type="button"
            onClick={() => setEditLayoutMode(!editLayoutMode)}
            className={`w-11 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors ${
              editLayoutMode ? 'bg-cyan-500' : 'bg-slate-800'
            }`}
          >
            <div
              className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                editLayoutMode ? 'translate-x-5' : 'translate-x-0'
              }`}
            />
          </button>
          <span className="text-[11px] font-mono text-slate-500">
            {editLayoutMode ? '(Mode Edit Aktif)' : '(Kunci Tata Letak)'}
          </span>
        </div>

        {editLayoutMode && (
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handleResetLayout}
              className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 rounded-lg text-xs font-mono font-bold flex items-center gap-1 cursor-pointer transition-colors"
              title="Kembalikan tata letak default"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>RESET TATA LETAK</span>
            </button>

            <button
              type="button"
              onClick={() => setShowAddWidgetDialog(true)}
              className="px-3 py-1 bg-cyan-950/80 hover:bg-cyan-900 border border-cyan-500/50 text-cyan-300 rounded-lg text-xs font-mono font-bold flex items-center gap-1.5 cursor-pointer shadow-sm transition-all"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>TAMBAH WIDGET GAUGE</span>
            </button>
          </div>
        )}
      </div>

      {/* SYMMETRICAL DYNAMIC GRID: Every single display gauge has IDENTICAL edit features */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-stretch">
        {widgets.map((widget, index) => {
          const currentPid = pids.find((p) => p.id === widget.pidId) || pids[0];
          const isAlert =
            isConnected &&
            ((currentPid.warnMax != null && currentPid.value >= currentPid.warnMax) ||
              (currentPid.warnMin != null && currentPid.value <= currentPid.warnMin));

          const valColor = !isConnected
            ? '#64748B' // Muted slate when disconnected
            : isAlert
            ? '#FF1744'
            : currentPid.colorOptimal || '#00E5FF';

          return (
            <div
              key={widget.id}
              className={`bg-[#11141A] border rounded-2xl relative p-4 transition-all flex flex-col justify-between shadow-xl ${
                isAlert ? 'border-red-500/70 shadow-red-500/10' : 'border-[#1E2530] hover:border-slate-700'
              } ${widget.displayType === 'dial' ? 'min-h-[290px] items-center justify-center' : 'min-h-[170px]'}`}
              style={{ transform: `scale(${widget.scale})`, transformOrigin: 'center center' }}
            >
              {/* UNIFORM EDIT TOOLBAR FOR EVERY GAUGE (Visible in Edit Mode) */}
              {editLayoutMode && (
                <div className="absolute top-2 right-2 z-20 flex items-center gap-1 bg-black/95 border border-slate-700 rounded-lg p-1 shadow-2xl">
                  {/* Model Toggle: DIAL vs CARD */}
                  <div className="flex items-center gap-0.5 pr-1 border-r border-slate-700">
                    <button
                      type="button"
                      onClick={() =>
                        handleUpdateWidget(widget.id, {
                          displayType: widget.displayType === 'dial' ? 'card' : 'dial',
                        })
                      }
                      className="px-1.5 py-0.5 rounded text-[9px] font-mono font-bold bg-cyan-950 text-cyan-300 border border-cyan-800 hover:bg-cyan-900 cursor-pointer"
                      title="Ganti model tampilan (Dial Bulat atau Kartu Digital)"
                    >
                      {widget.displayType === 'dial' ? 'DIAL' : 'CARD'}
                    </button>
                  </div>

                  {/* S / M / L Size Selectors */}
                  <div className="flex items-center gap-0.5 pr-1 border-r border-slate-700">
                    {[
                      { label: 'S', val: 'small' },
                      { label: 'M', val: 'medium' },
                      { label: 'L', val: 'large' },
                    ].map((sz) => (
                      <button
                        key={sz.label}
                        type="button"
                        onClick={() =>
                          handleUpdateWidget(widget.id, { size: sz.val as any })
                        }
                        className={`w-5 h-5 rounded text-[10px] font-mono font-black flex items-center justify-center cursor-pointer transition-colors ${
                          widget.size === sz.val
                            ? 'bg-cyan-400 text-slate-950 font-extrabold'
                            : 'text-slate-400 hover:text-white'
                        }`}
                      >
                        {sz.label}
                      </button>
                    ))}
                  </div>

                  {/* Zoom Controls */}
                  <button
                    type="button"
                    onClick={() =>
                      handleUpdateWidget(widget.id, {
                        scale: Math.max(0.5, Math.round((widget.scale - 0.1) * 10) / 10),
                      })
                    }
                    className="p-1 text-red-400 hover:text-red-300 cursor-pointer"
                    title="Zoom Out"
                  >
                    <ZoomOut className="w-3.5 h-3.5" />
                  </button>
                  <span className="text-[10px] font-mono font-bold text-slate-300 min-w-8 text-center">
                    {Math.round(widget.scale * 100)}%
                  </span>
                  <button
                    type="button"
                    onClick={() =>
                      handleUpdateWidget(widget.id, {
                        scale: Math.min(1.8, Math.round((widget.scale + 0.1) * 10) / 10),
                      })
                    }
                    className="p-1 text-emerald-400 hover:text-emerald-300 cursor-pointer"
                    title="Zoom In"
                  >
                    <ZoomIn className="w-3.5 h-3.5" />
                  </button>

                  {/* Move Left / Right */}
                  {index > 0 && (
                    <button
                      type="button"
                      onClick={() => handleMoveWidget(index, index - 1)}
                      className="p-1 text-cyan-400 hover:text-cyan-300 cursor-pointer"
                      title="Geser ke kiri / atas"
                    >
                      <ArrowLeft className="w-3.5 h-3.5" />
                    </button>
                  )}

                  {index < widgets.length - 1 && (
                    <button
                      type="button"
                      onClick={() => handleMoveWidget(index, index + 1)}
                      className="p-1 text-cyan-400 hover:text-cyan-300 cursor-pointer"
                      title="Geser ke kanan / bawah"
                    >
                      <ArrowRight className="w-3.5 h-3.5" />
                    </button>
                  )}

                  {/* Delete Button */}
                  <button
                    type="button"
                    onClick={() => handleDeleteWidget(widget.id)}
                    className="p-1 text-red-500 hover:text-red-400 cursor-pointer border-l border-slate-700 ml-0.5"
                    title="Hapus widget ini"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              )}

              {/* CARD CONTENT */}
              {widget.displayType === 'dial' ? (
                /* MODEL 1: CIRCULAR SWEEP DIAL GAUGE (55 SEGMENTS) */
                <div
                  className="w-full flex flex-col items-center justify-center cursor-pointer select-none"
                  onClick={() => setActiveSlotToChange(widget.id)}
                >
                  <span className="text-[10px] font-mono font-bold text-slate-500 uppercase tracking-widest mb-1">
                    PID: {currentPid.obdPid} · {currentPid.name.toUpperCase()}
                  </span>
                  <DialGauge
                    value={isConnected ? currentPid.value : 0}
                    min={currentPid.min}
                    max={currentPid.max}
                    title={currentPid.shortName}
                    unit={currentPid.unit}
                    warnThreshold={currentPid.warnMax || currentPid.max * 0.85}
                    mainColor={valColor}
                    className={
                      widget.size === 'small'
                        ? 'w-52 h-52'
                        : widget.size === 'large'
                        ? 'w-72 h-72 sm:w-80 sm:h-80'
                        : 'w-64 h-64'
                    }
                  />
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setPidToConfigure(currentPid);
                    }}
                    className="mt-2 text-[10px] font-mono text-cyan-400 hover:underline flex items-center gap-1 cursor-pointer"
                  >
                    <SettingsIcon className="w-3 h-3" />
                    <span>Ubah Skala & Batas Sensor</span>
                  </button>
                </div>
              ) : (
                /* MODEL 2: DIGITAL CARD GAUGE DISPLAY */
                <div
                  className="flex flex-col justify-between h-full cursor-pointer select-none"
                  onClick={() => setActiveSlotToChange(widget.id)}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono font-bold text-slate-300">
                        {currentPid.shortName.toUpperCase()}
                      </span>
                      {isAlert && <AlertTriangle className="w-3.5 h-3.5 text-red-500" />}
                    </div>

                    <div className="flex items-center gap-1.5">
                      <span className="text-[10px] font-mono text-slate-500 bg-slate-900 px-1.5 py-0.5 rounded border border-slate-800">
                        {currentPid.obdPid}
                      </span>
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setPidToConfigure(currentPid);
                        }}
                        className="text-slate-400 hover:text-cyan-400 p-0.5 cursor-pointer"
                        title="Edit Skala & Batas"
                      >
                        <SettingsIcon className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  {widget.size === 'medium' ? (
                    /* Medium Widget: Numeric on Left, Sparkline on Right */
                    <div className="flex items-center justify-between gap-4 py-2">
                      <div>
                        <div className="flex items-baseline gap-1.5">
                          <span
                            className="text-3xl font-mono font-extrabold"
                            style={{ color: valColor }}
                          >
                            {isConnected
                              ? typeof currentPid.value === 'number'
                                ? currentPid.value % 1 !== 0
                                  ? currentPid.value.toFixed(1)
                                  : Math.round(currentPid.value)
                                : currentPid.value
                              : 0}
                          </span>
                          <span className="text-xs font-mono text-slate-400">{currentPid.unit}</span>
                        </div>
                        <span
                          className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded uppercase mt-1 inline-block ${
                            !isConnected
                              ? 'bg-slate-900 text-slate-500'
                              : isAlert
                              ? 'bg-red-950 text-red-400 border border-red-800'
                              : 'bg-cyan-950 text-cyan-400'
                          }`}
                        >
                          {!isConnected ? 'STANDBY' : isAlert ? 'ALERT' : 'OPTIMAL'}
                        </span>
                      </div>

                      {/* Sparkline Canvas (Only active when connected) */}
                      <div className="w-36 h-12 bg-black/40 border border-white/5 rounded-lg p-1.5 flex flex-col justify-between">
                        <span className="text-[8px] font-mono text-slate-500 uppercase font-bold flex items-center justify-between">
                          <span>SPARKLINE</span>
                          {isConnected && <Activity className="w-2.5 h-2.5 text-cyan-400" />}
                        </span>
                        <svg viewBox="0 0 100 24" className="w-full h-6 overflow-visible">
                          {(() => {
                            if (!isConnected) {
                              return (
                                <line x1="0" y1="22" x2="100" y2="22" stroke="#334155" strokeDasharray="2 2" />
                              );
                            }
                            const pts = getSparklinePoints(currentPid.id);
                            if (pts.length < 2) return null;
                            const minVal = currentPid.min;
                            const maxVal = currentPid.max;
                            const range = maxVal - minVal || 1;
                            const pathData = pts
                              .map((v, i) => {
                                const x = (i / (pts.length - 1)) * 100;
                                const y = 24 - ((v - minVal) / range) * 22;
                                return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
                              })
                              .join(' ');
                            return (
                              <path
                                d={pathData}
                                fill="none"
                                stroke={valColor}
                                strokeWidth="2"
                                strokeLinecap="round"
                              />
                            );
                          })()}
                        </svg>
                      </div>
                    </div>
                  ) : widget.size === 'large' ? (
                    /* Large Widget: Expanded display with meter & min/max bounds */
                    <div className="flex flex-col gap-2 py-2">
                      <div className="flex items-baseline justify-between">
                        <div className="flex items-baseline gap-2">
                          <span
                            className="text-4xl font-mono font-black"
                            style={{ color: valColor }}
                          >
                            {isConnected
                              ? typeof currentPid.value === 'number'
                                ? currentPid.value % 1 !== 0
                                  ? currentPid.value.toFixed(1)
                                  : Math.round(currentPid.value)
                                : currentPid.value
                              : 0}
                          </span>
                          <span className="text-sm font-mono text-slate-400">{currentPid.unit}</span>
                        </div>
                        <span
                          className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded uppercase ${
                            !isConnected
                              ? 'bg-slate-900 text-slate-500'
                              : isAlert
                              ? 'bg-red-950 text-red-400 border border-red-800'
                              : 'bg-cyan-950 text-cyan-400'
                          }`}
                        >
                          {!isConnected ? 'STANDBY' : isAlert ? 'ALERT' : 'OPTIMAL'}
                        </span>
                      </div>

                      <div className="w-full bg-slate-800/80 h-2 rounded-full overflow-hidden">
                        <div
                          className="h-full transition-all duration-150 rounded-full"
                          style={{
                            width: isConnected
                              ? `${Math.max(
                                  0,
                                  Math.min(
                                    100,
                                    ((currentPid.value - currentPid.min) /
                                      (currentPid.max - currentPid.min)) *
                                      100
                                  )
                                )}%`
                              : '0%',
                            backgroundColor: valColor,
                          }}
                        />
                      </div>

                      <div className="flex justify-between text-[10px] font-mono text-slate-500 pt-1 border-t border-slate-800">
                        <span>MIN: {currentPid.min}</span>
                        <span>WARN: {currentPid.warnMax || '-'}</span>
                        <span>MAX: {currentPid.max}</span>
                      </div>
                    </div>
                  ) : (
                    /* Small Widget: Compact */
                    <div className="flex flex-col gap-1 py-1">
                      <div className="flex items-baseline gap-2">
                        <span className="text-2xl font-mono font-black text-white">
                          {isConnected
                            ? typeof currentPid.value === 'number'
                              ? currentPid.value % 1 !== 0
                                ? currentPid.value.toFixed(1)
                                : Math.round(currentPid.value)
                              : currentPid.value
                            : 0}
                        </span>
                        <span className="text-xs font-mono font-semibold" style={{ color: valColor }}>
                          {currentPid.unit}
                        </span>
                      </div>

                      <div className="w-full bg-slate-800/80 h-1.5 rounded-full overflow-hidden mt-1">
                        <div
                          className="h-full transition-all duration-150 rounded-full"
                          style={{
                            width: isConnected
                              ? `${Math.max(
                                  0,
                                  Math.min(
                                    100,
                                    ((currentPid.value - currentPid.min) /
                                      (currentPid.max - currentPid.min)) *
                                      100
                                  )
                                )}%`
                              : '0%',
                            backgroundColor: valColor,
                          }}
                        />
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* DIALOG 1: ADD WIDGET GAUGE MODAL */}
      {showAddWidgetDialog && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#151922] border border-[#2E394A] rounded-2xl max-w-md w-full p-5 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-sm font-mono font-bold text-cyan-400 uppercase">
                TAMBAH DISPLAY GAUGE BARU
              </h3>
              <button
                type="button"
                onClick={() => setShowAddWidgetDialog(false)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-[11px] font-mono text-slate-400">
              Pilih sensor yang ingin Anda tampilkan sebagai display gauge baru:
            </p>

            <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
              {pids.map((p) => (
                <div
                  key={p.id}
                  onClick={() => {
                    const newWidget: WidgetConfig = {
                      id: `w_${p.id}_${Date.now()}`,
                      pidId: p.id,
                      displayType: p.id === 'rpm' || p.id === 'speed' ? 'dial' : 'card',
                      size: 'medium',
                      scale: 1.0,
                    };
                    saveWidgets([...widgets, newWidget]);
                    setShowAddWidgetDialog(false);
                  }}
                  className="bg-[#1B212D] hover:bg-cyan-950/40 border border-slate-700 hover:border-cyan-500/50 p-3 rounded-xl flex items-center justify-between cursor-pointer transition-colors"
                >
                  <div>
                    <h4 className="text-xs font-mono font-bold text-white">{p.name}</h4>
                    <span className="text-[10px] font-mono text-slate-400">
                      {p.shortName} · {p.category} · {p.obdPid}
                    </span>
                  </div>
                  <Plus className="w-4 h-4 text-cyan-400" />
                </div>
              ))}
            </div>

            <button
              type="button"
              onClick={() => setShowAddWidgetDialog(false)}
              className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs font-bold rounded-lg cursor-pointer"
            >
              TUTUP
            </button>
          </div>
        </div>
      )}

      {/* DIALOG 2: SELECT / REPLACE SENSOR FOR THIS GAUGE */}
      {activeSlotToChange != null && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#151922] border border-[#2E394A] rounded-2xl max-w-md w-full p-5 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="text-sm font-mono font-bold text-cyan-400 uppercase">
                GANTI SENSOR UNTUK GAUGE INI
              </h3>
              <button
                type="button"
                onClick={() => setActiveSlotToChange(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-2 max-h-80 overflow-y-auto pr-1">
              {pids.map((p) => (
                <div
                  key={p.id}
                  onClick={() => {
                    handleUpdateWidget(activeSlotToChange, { pidId: p.id });
                    setActiveSlotToChange(null);
                  }}
                  className="bg-[#1B212D] hover:bg-cyan-950/40 border border-slate-700 hover:border-cyan-500/50 p-3 rounded-xl flex items-center justify-between cursor-pointer transition-colors"
                >
                  <div>
                    <h4 className="text-xs font-mono font-bold text-white">{p.name}</h4>
                    <span className="text-[10px] font-mono text-slate-400">
                      {p.shortName} · {p.category}
                    </span>
                  </div>
                  <span className="text-xs font-mono font-bold text-cyan-400">
                    {isConnected ? (typeof p.value === 'number' ? p.value.toFixed(1) : p.value) : 0} {p.unit}
                  </span>
                </div>
              ))}
            </div>

            <button
              type="button"
              onClick={() => setActiveSlotToChange(null)}
              className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-xs font-bold rounded-lg cursor-pointer"
            >
              BATAL
            </button>
          </div>
        </div>
      )}

      {/* DIALOG 3: CONFIGURE PID SCALE & BOUNDARIES */}
      {pidToConfigure != null && (
        <ConfigurePidModal
          pid={pidToConfigure}
          onClose={() => setPidToConfigure(null)}
          onSave={(min, max, warnMin, warnMax, optCol, highCol) => {
            updatePIDConfig(pidToConfigure.id, min, max, warnMin, warnMax, optCol, undefined, highCol);
            setPidToConfigure(null);
          }}
        />
      )}
    </div>
  );
};

interface ConfigurePidModalProps {
  pid: PID;
  onClose: () => void;
  onSave: (min: number, max: number, warnMin?: number, warnMax?: number, optCol?: string, highCol?: string) => void;
}

const ConfigurePidModal: React.FC<ConfigurePidModalProps> = ({ pid, onClose, onSave }) => {
  const [minVal, setMinVal] = useState(pid.min.toString());
  const [maxVal, setMaxVal] = useState(pid.max.toString());
  const [warnMinVal, setWarnMinVal] = useState(pid.warnMin?.toString() || '');
  const [warnMaxVal, setWarnMaxVal] = useState(pid.warnMax?.toString() || '');
  const [optColor, setOptColor] = useState(pid.colorOptimal || '#00E5FF');
  const [highColor, setHighColor] = useState(pid.colorHigh || '#FF1744');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSave = () => {
    const minNum = parseFloat(minVal);
    const maxNum = parseFloat(maxVal);
    if (isNaN(minNum) || isNaN(maxNum)) {
      setErrorMsg('Batas minimum dan maksimum harus berupa angka yang valid.');
      return;
    }
    if (minNum >= maxNum) {
      setErrorMsg('Batas minimum harus lebih kecil dari batas maksimum.');
      return;
    }
    const warnMinNum = warnMinVal.trim() !== '' ? parseFloat(warnMinVal) : undefined;
    const warnMaxNum = warnMaxVal.trim() !== '' ? parseFloat(warnMaxVal) : undefined;

    onSave(minNum, maxNum, warnMinNum, warnMaxNum, optColor, highColor);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-[#151922] border border-[#2E394A] rounded-2xl max-w-md w-full p-5 shadow-2xl space-y-4 font-mono text-xs">
        <div className="flex items-center justify-between border-b border-slate-800 pb-2">
          <div>
            <h3 className="text-sm font-bold text-cyan-400 uppercase">
              EDIT SKALA & BATAS: {pid.shortName}
            </h3>
            <span className="text-[10px] text-slate-400">Atur batas min, max, redline, dan warna</span>
          </div>
          <button type="button" onClick={onClose} className="text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {errorMsg && (
          <div className="p-2 bg-red-950/80 border border-red-700 text-red-300 rounded text-[11px]">
            {errorMsg}
          </div>
        )}

        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-400 block mb-1">BATAS MIN ({pid.unit})</label>
              <input
                type="number"
                value={minVal}
                onChange={(e) => setMinVal(e.target.value)}
                className="w-full bg-[#1D222E] border border-slate-700 rounded px-2.5 py-1.5 text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
            <div>
              <label className="text-slate-400 block mb-1">BATAS MAX ({pid.unit})</label>
              <input
                type="number"
                value={maxVal}
                onChange={(e) => setMaxVal(e.target.value)}
                className="w-full bg-[#1D222E] border border-slate-700 rounded px-2.5 py-1.5 text-white focus:outline-none focus:border-cyan-400"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-400 block mb-1">BATAS PERINGATAN BAWAH</label>
              <input
                type="number"
                value={warnMinVal}
                placeholder="Kosongkan jika tidak ada"
                onChange={(e) => setWarnMinVal(e.target.value)}
                className="w-full bg-[#1D222E] border border-slate-700 rounded px-2.5 py-1.5 text-white focus:outline-none focus:border-cyan-400 placeholder:text-slate-600"
              />
            </div>
            <div>
              <label className="text-slate-400 block mb-1">REDLINE / MAX WARN</label>
              <input
                type="number"
                value={warnMaxVal}
                placeholder="Misal 9500"
                onChange={(e) => setWarnMaxVal(e.target.value)}
                className="w-full bg-[#1D222E] border border-slate-700 rounded px-2.5 py-1.5 text-white focus:outline-none focus:border-cyan-400 placeholder:text-slate-600"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="text-slate-400 block mb-1">WARNA OPTIMAL (HEX)</label>
              <div className="flex items-center gap-2">
                <input
                  type="color"
                  value={optColor}
                  onChange={(e) => setOptColor(e.target.value)}
                  className="w-8 h-8 rounded border border-slate-700 bg-transparent cursor-pointer"
                />
                <input
                  type="text"
                  value={optColor}
                  onChange={(e) => setOptColor(e.target.value)}
                  className="flex-1 bg-[#1D222E] border border-slate-700 rounded px-2 py-1 text-white uppercase text-[11px]"
                />
              </div>
            </div>

            <div>
              <label className="text-slate-400 block mb-1">WARNA REDLINE (HEX)</label>
              <div className="flex items-center gap-2">
                <input
                  type="color"
                  value={highColor}
                  onChange={(e) => setHighColor(e.target.value)}
                  className="w-8 h-8 rounded border border-slate-700 bg-transparent cursor-pointer"
                />
                <input
                  type="text"
                  value={highColor}
                  onChange={(e) => setHighColor(e.target.value)}
                  className="flex-1 bg-[#1D222E] border border-slate-700 rounded px-2 py-1 text-white uppercase text-[11px]"
                />
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-3 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold cursor-pointer"
          >
            Batal
          </button>
          <button
            type="button"
            onClick={handleSave}
            className="px-4 py-1.5 rounded bg-cyan-400 hover:bg-cyan-300 text-slate-950 font-bold cursor-pointer shadow-md shadow-cyan-400/20"
          >
            Simpan Perubahan
          </button>
        </div>
      </div>
    </div>
  );
};

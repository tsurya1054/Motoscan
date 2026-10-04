import React from 'react';
import { useOBDContext } from '../OBDContext';
import { PowerOff } from 'lucide-react';

export const LiveDataView: React.FC = () => {
  const { pids, graphData, isConnected } = useOBDContext();

  const renderRpmThrottleGraph = () => {
    if (!isConnected || graphData.length < 2) {
      return (
        <div className="h-28 flex flex-col items-center justify-center border border-dashed border-slate-800 rounded-lg text-slate-500 font-mono text-xs">
          <span>DATA GRAFIK STANDBY (OBD2 TERPUTUS)</span>
          <span className="text-[10px] text-slate-600 mt-1">
            Hubungkan perangkat ELM327 untuk melihat grafik RPM vs Throttle
          </span>
        </div>
      );
    }

    const width = 600;
    const height = 120;
    const totalPoints = graphData.length;

    const rpmPoints = graphData
      .map((d, i) => {
        const x = (i / (totalPoints - 1)) * width;
        const normRpm = Math.max(0, Math.min(1, (d.rpm - 1000) / 10500));
        const y = height - normRpm * (height - 20) - 10;
        return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');

    const throttlePoints = graphData
      .map((d, i) => {
        const x = (i / (totalPoints - 1)) * width;
        const normThrottle = Math.max(0, Math.min(1, d.throttle / 100));
        const y = height - normThrottle * (height - 20) - 10;
        return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');

    return (
      <svg viewBox="0 0 600 120" className="w-full h-28 overflow-visible" preserveAspectRatio="none">
        <line x1="0" y1="30" x2="600" y2="30" stroke="#1E2530" strokeDasharray="3 3" />
        <line x1="0" y1="60" x2="600" y2="60" stroke="#1E2530" strokeDasharray="3 3" />
        <line x1="0" y1="90" x2="600" y2="90" stroke="#1E2530" strokeDasharray="3 3" />

        <path d={rpmPoints} fill="none" stroke="#00E5FF" strokeWidth="2.5" strokeLinecap="round" />
        <path d={throttlePoints} fill="none" stroke="#FF9100" strokeWidth="2" strokeLinecap="round" />
      </svg>
    );
  };

  return (
    <div className="flex flex-col gap-4 w-full">
      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
        <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider">
          ALL OBD2 SENSOR MONITOR (LIVE DATA)
        </h2>
        <span className="text-xs font-mono text-slate-500">
          {pids.length} ACTIVE PIDs · {isConnected ? 'LIVE FEED' : 'STANDBY (0)'}
        </span>
      </div>

      {!isConnected && (
        <div className="bg-[#11141A] border border-slate-800 rounded-xl px-4 py-2.5 flex items-center gap-2.5 text-xs font-mono text-slate-400">
          <PowerOff className="w-4 h-4 text-slate-500 shrink-0" />
          <span>
            Sensor dalam kondisi standby (nilai 0). Tidak ada aliran data sampai Anda mengaktifkan koneksi ke ECU motor.
          </span>
        </div>
      )}

      {/* SENSORS GRID */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {pids.map((pid) => {
          const val = isConnected ? pid.value : 0;
          const ratio = isConnected
            ? Math.max(0, Math.min(100, ((val - pid.min) / (pid.max - pid.min)) * 100))
            : 0;
          const isWarn = isConnected && (pid.warnMax ? val >= pid.warnMax : false);

          return (
            <div
              key={pid.id}
              className="bg-[#11141A] border border-[#1E2530] hover:border-cyan-500/40 rounded-xl p-3.5 flex flex-col justify-between shadow-md transition-all"
            >
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono font-bold text-slate-300">
                  {pid.name}
                </span>
                <span className="text-[9px] font-mono text-slate-500 px-1 py-0.5 rounded bg-slate-900 border border-slate-800">
                  {pid.obdPid}
                </span>
              </div>

              <div className="flex items-baseline justify-between mt-2">
                <span
                  className="text-2xl font-mono font-extrabold"
                  style={{
                    color: !isConnected
                      ? '#64748B'
                      : isWarn
                      ? '#FF1744'
                      : pid.colorOptimal || '#00E5FF',
                  }}
                >
                  {typeof val === 'number'
                    ? val % 1 !== 0
                      ? val.toFixed(1)
                      : Math.round(val)
                    : val}
                </span>
                <span className="text-[11px] font-mono text-cyan-400 font-semibold">
                  {pid.unit}
                </span>
              </div>

              {/* Progress Level Bar */}
              <div className="w-full bg-slate-800/80 h-1.5 rounded-full overflow-hidden mt-2">
                <div
                  className="h-full transition-all duration-150 rounded-full"
                  style={{
                    width: `${ratio}%`,
                    backgroundColor: !isConnected
                      ? '#334155'
                      : isWarn
                      ? '#FF1744'
                      : pid.colorOptimal || '#00E5FF',
                  }}
                />
              </div>
            </div>
          );
        })}
      </div>

      {/* LIVE GRAPH FEED (RPM vs THROTTLE) */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl p-4 shadow-xl flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <span className="text-xs font-mono font-bold text-slate-300">
            LIVE GRAPH FEED (RPM vs THROTTLE)
          </span>
          <div className="flex items-center gap-3 text-[10px] font-mono">
            <div className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-sm bg-[#00E5FF]" />
              <span className="text-slate-400">RPM (Cyan)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-sm bg-[#FF9100]" />
              <span className="text-slate-400">Throttle (Orange)</span>
            </div>
          </div>
        </div>

        <div className="w-full pt-2">{renderRpmThrottleGraph()}</div>
      </div>
    </div>
  );
};

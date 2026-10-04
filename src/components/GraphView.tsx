import React from 'react';
import { useOBDContext } from '../OBDContext';
import { PowerOff } from 'lucide-react';

export const GraphView: React.FC = () => {
  const { graphData, isConnected } = useOBDContext();
  const pointsCount = isConnected ? graphData.length : 0;

  const renderLine = (key: 'rpm' | 'speed' | 'coolant' | 'throttle', maxVal: number) => {
    if (!isConnected || pointsCount < 2) return '';
    const width = 600;
    const height = 220;

    return graphData
      .map((d, i) => {
        const x = (i / (pointsCount - 1)) * width;
        const val = d[key];
        const y = height - (Math.min(maxVal, Math.max(0, val)) / maxVal) * (height - 20) - 10;
        return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
      })
      .join(' ');
  };

  const latest = isConnected && graphData.length > 0
    ? graphData[graphData.length - 1]
    : { rpm: 0, speed: 0, coolant: 0, throttle: 0 };

  return (
    <div className="flex flex-col gap-4 w-full">
      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
        <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider">
          LIVE PERFORMANCE TELEMETRY GRAPH
        </h2>
        <span className="text-xs font-mono text-slate-500">
          {isConnected ? 'REAL-TIME (50 DATA POINTS)' : 'STANDBY (0)'}
        </span>
      </div>

      {!isConnected && (
        <div className="bg-[#11141A] border border-slate-800 rounded-xl px-4 py-2.5 flex items-center gap-2.5 text-xs font-mono text-slate-400">
          <PowerOff className="w-4 h-4 text-slate-500 shrink-0" />
          <span>
            Grafik dalam mode standby. Hubungkan ELM327 OBD2 untuk melihat plotting multi-saluran secara langsung.
          </span>
        </div>
      )}

      {/* Legend & Real-time Readouts */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
        <div className="bg-[#11141A] border border-[#1E2530] p-2.5 rounded-lg flex items-center justify-between">
          <span className="text-xs font-mono text-[#00E5FF] font-bold">RPM</span>
          <span className="text-sm font-mono font-extrabold text-white">{latest.rpm}</span>
        </div>
        <div className="bg-[#11141A] border border-[#1E2530] p-2.5 rounded-lg flex items-center justify-between">
          <span className="text-xs font-mono text-[#00C853] font-bold">SPEED</span>
          <span className="text-sm font-mono font-extrabold text-white">{latest.speed} km/h</span>
        </div>
        <div className="bg-[#11141A] border border-[#1E2530] p-2.5 rounded-lg flex items-center justify-between">
          <span className="text-xs font-mono text-[#FFD600] font-bold">THROTTLE</span>
          <span className="text-sm font-mono font-extrabold text-white">{latest.throttle}%</span>
        </div>
        <div className="bg-[#11141A] border border-[#1E2530] p-2.5 rounded-lg flex items-center justify-between">
          <span className="text-xs font-mono text-[#FF1744] font-bold">COOLANT</span>
          <span className="text-sm font-mono font-extrabold text-white">{latest.coolant}°C</span>
        </div>
      </div>

      {/* Main SVG Line Graph Canvas */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl p-4 shadow-xl overflow-hidden">
        <div className="relative w-full aspect-[21/9] min-h-[240px]">
          <svg viewBox="0 0 600 220" className="w-full h-full overflow-visible" preserveAspectRatio="none">
            {/* Grid Lines */}
            <line x1="0" y1="40" x2="600" y2="40" stroke="#1E2530" strokeDasharray="3 3" />
            <line x1="0" y1="95" x2="600" y2="95" stroke="#1E2530" strokeDasharray="3 3" />
            <line x1="0" y1="150" x2="600" y2="150" stroke="#1E2530" strokeDasharray="3 3" />
            <line x1="0" y1="205" x2="600" y2="205" stroke="#252F3D" />

            {/* Channels (Only render active plot if connected) */}
            {isConnected && (
              <>
                {/* RPM Line (Cyan) */}
                <path
                  d={renderLine('rpm', 10000)}
                  fill="none"
                  stroke="#00E5FF"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  className="transition-all duration-100"
                />

                {/* Speed Line (Green) */}
                <path
                  d={renderLine('speed', 160)}
                  fill="none"
                  stroke="#00C853"
                  strokeWidth="2"
                  strokeLinecap="round"
                  className="transition-all duration-100"
                />

                {/* Throttle Line (Yellow) */}
                <path
                  d={renderLine('throttle', 100)}
                  fill="none"
                  stroke="#FFD600"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  className="transition-all duration-100"
                />

                {/* Coolant Line (Red) */}
                <path
                  d={renderLine('coolant', 130)}
                  fill="none"
                  stroke="#FF1744"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  className="transition-all duration-100"
                />
              </>
            )}
          </svg>
        </div>
      </div>
    </div>
  );
};

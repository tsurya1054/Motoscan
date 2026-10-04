import React from 'react';

interface DialGaugeProps {
  value: number;
  min: number;
  max: number;
  title: string;
  unit: string;
  className?: string;
  mainColor?: string;
  warnThreshold?: number;
  warnColor?: string;
}

export const DialGauge: React.FC<DialGaugeProps> = ({
  value,
  min,
  max,
  title,
  unit,
  className = 'w-64 h-64',
  mainColor = '#00E5FF',
  warnThreshold = 10000,
  warnColor = '#FF1744',
}) => {
  // START EXACTLY FROM BOTTOM CENTER: 6 o'clock (180 degrees)
  // Sweep clockwise around the gauge (330 degrees sweep, ending at 5 o'clock)
  const startAngle = 180;
  const sweepAngle = 330;

  const range = max - min;
  const ratio = range > 0 ? Math.max(0, Math.min(1, (value - min) / range)) : 0;

  // 55 Segmented LED blocks forming the high-tech digital gauge ring
  const totalSegments = 55;
  const segmentSweep = sweepAngle / totalSegments;
  const gapAngle = 1.2;
  const drawSweep = segmentSweep - gapAngle;

  const center = 120;
  const radius = 95;

  // Clock coordinates:
  // 0 deg = 12 o'clock (Top: cx, cy - r)
  // 90 deg = 3 o'clock (Right: cx + r, cy)
  // 180 deg = 6 o'clock (Bottom Center: cx, cy + r)
  // 270 deg = 9 o'clock (Left: cx - r, cy)
  const clockToCartesian = (cx: number, cy: number, r: number, angleDeg: number) => {
    const rad = (angleDeg * Math.PI) / 180.0;
    return {
      x: cx + r * Math.sin(rad),
      y: cy - r * Math.cos(rad),
    };
  };

  // Draws an arc from startA to endA sweeping clockwise
  const describeArc = (cx: number, cy: number, r: number, startA: number, endA: number) => {
    const start = clockToCartesian(cx, cy, r, startA);
    const end = clockToCartesian(cx, cy, r, endA);
    const delta = endA - startA;
    const largeArcFlag = delta > 180 ? 1 : 0;
    return `M ${start.x.toFixed(2)} ${start.y.toFixed(2)} A ${r} ${r} 0 ${largeArcFlag} 1 ${end.x.toFixed(2)} ${end.y.toFixed(2)}`;
  };

  const isWarnActive = warnThreshold < (min + max) / 2 ? value <= warnThreshold : value >= warnThreshold;
  const currentColor = isWarnActive ? warnColor : mainColor;

  return (
    <div className={`relative flex flex-col items-center justify-center select-none ${className}`}>
      <svg viewBox="0 0 240 240" className="w-full h-full drop-shadow-[0_0_15px_rgba(0,229,255,0.15)]">
        {/* Background track circle */}
        <circle cx={center} cy={center} r={radius} fill="none" stroke="#13171F" strokeWidth="12" />

        {/* 55 Segments starting from 6 o'clock (180 deg) and sweeping clockwise */}
        {Array.from({ length: totalSegments }).map((_, i) => {
          const pct = i / (totalSegments - 1);
          const segStart = startAngle + i * segmentSweep + gapAngle / 2;
          const segEnd = segStart + drawSweep;

          const isActive = ratio > 0 && pct <= ratio;
          const valAtSeg = min + (max - min) * pct;
          const isLowLimit = warnThreshold < (min + max) / 2;
          const isWarnZone = isLowLimit ? valAtSeg <= warnThreshold : valAtSeg >= warnThreshold;

          const activeColor = isWarnZone ? warnColor : mainColor;
          const inactiveColor = isWarnZone ? 'rgba(255, 23, 68, 0.12)' : 'rgba(0, 229, 255, 0.08)';
          const color = isActive ? activeColor : inactiveColor;

          return (
            <path
              key={i}
              d={describeArc(center, center, radius, segStart, segEnd)}
              fill="none"
              stroke={color}
              strokeWidth="10"
              strokeLinecap="butt"
              className="transition-colors duration-100"
            />
          );
        })}

        {/* Start Point Marker (6 o'clock / Pukul 6 / Tengah Bawah) */}
        <line
          x1={center}
          y1={center + radius - 7}
          x2={center}
          y2={center + radius + 7}
          stroke="#00E5FF"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
        <text
          x={center}
          y={center + radius + 18}
          textAnchor="middle"
          fill="#00E5FF"
          fontSize="8"
          fontFamily="monospace"
          fontWeight="bold"
        >
          START (0)
        </text>

        {/* Max / Redline Marker at End of Arc (5 o'clock) */}
        {(() => {
          const endTick = clockToCartesian(center, center, radius, startAngle + sweepAngle);
          return (
            <circle
              cx={endTick.x}
              cy={endTick.y}
              r="2.5"
              fill={warnColor}
            />
          );
        })()}
      </svg>

      {/* Central Digital Readout */}
      <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
        <span className="text-[11px] font-mono font-bold tracking-widest text-slate-400 uppercase">
          {title}
        </span>
        <span
          className="text-4xl sm:text-5xl font-mono font-extrabold tracking-tight transition-colors duration-150"
          style={{ color: currentColor, textShadow: `0 0 20px ${currentColor}60` }}
        >
          {Math.round(value).toLocaleString()}
        </span>
        <span className="text-xs font-mono font-bold text-slate-400 mt-0.5">
          {unit}
        </span>
      </div>
    </div>
  );
};

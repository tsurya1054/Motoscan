import React from 'react';
import { useOBDContext } from '../OBDContext';
import { CheckCircle2, XCircle } from 'lucide-react';

export const ReadinessMonitorView: React.FC = () => {
  const { readiness } = useOBDContext();

  return (
    <div className="space-y-4 w-full">
      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
        <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider">
          I/M EMISSION READINESS MONITORS
        </h2>
        <span className="text-xs font-mono text-slate-500">OBD2 COMPLIANCE</span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
        {readiness.map((item) => (
          <div
            key={item.id}
            className="bg-[#11141A] border border-[#1E2530] p-3.5 rounded-xl flex items-center justify-between shadow-sm"
          >
            <div>
              <h4 className="text-xs font-mono font-bold text-slate-200">{item.name}</h4>
              <span className="text-[10px] text-slate-500 font-mono">{item.nameIndo}</span>
            </div>
            <span
              className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border flex items-center gap-1 ${
                item.status === 'READY'
                  ? 'bg-emerald-950 text-emerald-400 border-emerald-800'
                  : 'bg-red-950 text-red-400 border-red-800'
              }`}
            >
              {item.status === 'READY' ? <CheckCircle2 className="w-3 h-3" /> : <XCircle className="w-3 h-3" />}
              {item.status}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

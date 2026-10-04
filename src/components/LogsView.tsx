import React from 'react';
import { useOBDContext } from '../OBDContext';
import { LogFile } from '../types';
import { FileText, Star, Trash2, Download } from 'lucide-react';

export const LogsView: React.FC = () => {
  const { logs, deleteLog, toggleFavoriteLog } = useOBDContext();

  const handleDownloadLog = (log: LogFile) => {
    const blob = new Blob([log.content || 'Timestamp,RPM,Speed\n'], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = log.name;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="space-y-3 w-full">
      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
        <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider">
          SAVED TELEMETRY LOGS ({logs.length})
        </h2>
        <span className="text-xs font-mono text-slate-500">CSV FORMAT</span>
      </div>

      {logs.length === 0 ? (
        <div className="bg-[#11141A] border border-[#1E2530] rounded-xl p-8 text-center text-slate-500 font-mono text-xs">
          Belum ada file log tersimpan. Tekan "REKAM LOG" pada Dashboard untuk merekam data OBD2.
        </div>
      ) : (
        logs.map((log) => (
          <div
            key={log.name}
            className="bg-[#11141A] border border-[#1E2530] hover:border-slate-700 rounded-xl p-3.5 flex items-center justify-between gap-3 shadow-md transition-all"
          >
            <div className="flex items-center gap-3">
              <FileText className="w-5 h-5 text-cyan-400 shrink-0" />
              <div>
                <h4 className="text-xs font-mono font-bold text-slate-200">
                  {log.name}
                </h4>
                <div className="flex items-center gap-2 text-[10px] font-mono text-slate-500 mt-0.5">
                  <span>{log.date}</span>
                  <span>·</span>
                  <span>{log.size}</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => toggleFavoriteLog(log.name)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-yellow-400 transition-colors cursor-pointer"
              >
                <Star className={`w-4 h-4 ${log.isFavorite ? 'fill-yellow-400 text-yellow-400' : ''}`} />
              </button>

              <button
                type="button"
                onClick={() => handleDownloadLog(log)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-400 transition-colors cursor-pointer"
                title="Download CSV"
              >
                <Download className="w-4 h-4" />
              </button>

              <button
                type="button"
                onClick={() => deleteLog(log.name)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-red-400 transition-colors cursor-pointer"
                title="Delete Log"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
};

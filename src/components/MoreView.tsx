import React, { useState } from 'react';
import { LogsView } from './LogsView';
import { ReadinessMonitorView } from './ReadinessMonitorView';
import { SettingsView } from './SettingsView';
import { FileText, CheckCircle2, Settings } from 'lucide-react';

export const MoreView: React.FC = () => {
  const [subTab, setSubTab] = useState<'logs' | 'readiness' | 'settings'>('logs');

  return (
    <div className="flex flex-col gap-4 w-full">
      {/* Sub Tabs Navigation */}
      <div className="flex items-center gap-1 p-1 bg-[#11141A] border border-[#1E2530] rounded-xl">
        <button
          type="button"
          onClick={() => setSubTab('logs')}
          className={`flex-1 py-2 px-3 text-xs font-mono font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            subTab === 'logs' ? 'bg-[#00E5FF] text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
          }`}
        >
          <FileText className="w-3.5 h-3.5" />
          <span>DATA LOGS</span>
        </button>

        <button
          type="button"
          onClick={() => setSubTab('readiness')}
          className={`flex-1 py-2 px-3 text-xs font-mono font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            subTab === 'readiness' ? 'bg-[#00E5FF] text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
          }`}
        >
          <CheckCircle2 className="w-3.5 h-3.5" />
          <span>EMISSION READINESS</span>
        </button>

        <button
          type="button"
          onClick={() => setSubTab('settings')}
          className={`flex-1 py-2 px-3 text-xs font-mono font-bold rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
            subTab === 'settings' ? 'bg-[#00E5FF] text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
          }`}
        >
          <Settings className="w-3.5 h-3.5" />
          <span>APP SETTINGS</span>
        </button>
      </div>

      {subTab === 'logs' && <LogsView />}
      {subTab === 'readiness' && <ReadinessMonitorView />}
      {subTab === 'settings' && <SettingsView />}
    </div>
  );
};

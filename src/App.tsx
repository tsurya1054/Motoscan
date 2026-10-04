import React, { useState } from 'react';
import { OBDProvider } from './OBDContext';
import { GaugeView } from './components/GaugeView';
import { LiveDataView } from './components/LiveDataView';
import { DtcView } from './components/DtcView';
import { GraphView } from './components/GraphView';
import { MoreView } from './components/MoreView';
import {
  Gauge,
  Sliders,
  AlertTriangle,
  LineChart,
  MoreHorizontal,
} from 'lucide-react';

function MotoScanApp() {
  const [selectedTab, setSelectedTab] = useState<number>(0);

  return (
    <div className="min-h-screen bg-[#090A0F] text-slate-100 flex flex-col font-sans selection:bg-cyan-500 selection:text-slate-950">
      {/* Top Header Bar */}
      <header className="w-full border-b border-[#1E2530] bg-[#11141A]/95 backdrop-blur-md sticky top-0 z-40">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-7 h-7 rounded-lg bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
              <Gauge className="w-4 h-4" />
            </div>
            <div>
              <span className="text-base font-extrabold tracking-tight text-white font-mono">
                MotoScan ADV
              </span>
              <span className="hidden sm:inline text-[11px] font-mono text-slate-500 ml-2">
                · OBD2 Motorcycle Telemetry & Diagnostics
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Screen Content */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 py-5 flex flex-col gap-5">
        {selectedTab === 0 && <GaugeView />}
        {selectedTab === 1 && <LiveDataView />}
        {selectedTab === 2 && <DtcView />}
        {selectedTab === 3 && <GraphView />}
        {selectedTab === 4 && <MoreView />}
      </main>

      {/* Bottom Navigation Bar (Matching Android MainActivity.kt) */}
      <div className="w-full bg-[#11141A] border-t border-[#1E2530] sticky bottom-0 z-30">
        <div className="max-w-md mx-auto grid grid-cols-5 py-1">
          {[
            { id: 0, label: 'Dashboard', icon: Gauge },
            { id: 1, label: 'Live Data', icon: Sliders },
            { id: 2, label: 'DTC', icon: AlertTriangle },
            { id: 3, label: 'Graph', icon: LineChart },
            { id: 4, label: 'More', icon: MoreHorizontal },
          ].map((tab) => {
            const isSelected = selectedTab === tab.id;
            const IconComp = tab.icon;
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setSelectedTab(tab.id)}
                className="flex flex-col items-center justify-center py-2 px-1 cursor-pointer transition-colors"
              >
                <IconComp
                  className={`w-5 h-5 transition-colors ${
                    isSelected ? 'text-[#00E5FF]' : 'text-slate-500 hover:text-slate-300'
                  }`}
                />
                <span
                  className={`text-[10px] font-mono mt-1 ${
                    isSelected ? 'text-[#00E5FF] font-extrabold' : 'text-slate-500'
                  }`}
                >
                  {tab.label}
                </span>
                <div
                  className={`w-4 h-0.5 mt-1 rounded-full ${
                    isSelected ? 'bg-[#00E5FF]' : 'bg-transparent'
                  }`}
                />
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export function App() {
  return (
    <OBDProvider>
      <MotoScanApp />
    </OBDProvider>
  );
}

export default App;

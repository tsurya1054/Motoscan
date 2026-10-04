import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { PID, DTC, ReadinessItem, LogFile, OBD2Settings } from './types';
import {
  INITIAL_PIDS,
  INITIAL_READINESS,
  INITIAL_LOGS,
  INITIAL_SETTINGS,
  getDTCsForVehicle,
} from './data';

interface OBDContextType {
  pids: PID[];
  dtcs: DTC[];
  readiness: ReadinessItem[];
  logs: LogFile[];
  settings: OBD2Settings;
  isConnected: boolean;
  isRecording: boolean;
  graphData: { rpm: number; speed: number; coolant: number; throttle: number }[];
  setIsConnected: (val: boolean) => void;
  updateSettings: (newSettings: OBD2Settings) => void;
  toggleRecording: () => void;
  clearDtcs: () => void;
  refreshDtcs: () => void;
  deleteLog: (name: string) => void;
  toggleFavoriteLog: (name: string) => void;
  updatePIDConfig: (
    id: string,
    min: number,
    max: number,
    warnMin?: number,
    warnMax?: number,
    colorOptimal?: string,
    colorLow?: string,
    colorHigh?: string
  ) => void;
}

const OBDContext = createContext<OBDContextType | undefined>(undefined);

export const OBDProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // When app opens, clean reset: all PIDs at 0 resting position
  const [pids, setPids] = useState<PID[]>(INITIAL_PIDS);

  const [settings, setSettings] = useState<OBD2Settings>(() => {
    try {
      const saved = localStorage.getItem('motoscan_settings');
      if (saved) {
        const parsed = JSON.parse(saved);
        return {
          ...INITIAL_SETTINGS,
          ...parsed,
          specs: {
            ...INITIAL_SETTINGS.specs,
            ...(parsed.specs || {}),
          },
        };
      }
    } catch {
      // Fallback
    }
    return INITIAL_SETTINGS;
  });

  // Dynamic DTCs strictly respecting the vehicle's cylinder count
  const [dtcs, setDtcs] = useState<DTC[]>(() =>
    getDTCsForVehicle(settings.specs?.cylinderCount ?? 1)
  );

  const [readiness] = useState<ReadinessItem[]>(INITIAL_READINESS);
  const [logs, setLogs] = useState<LogFile[]>(INITIAL_LOGS);

  // App starts in CLEAN DISCONNECTED state: No data movement until user connects!
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [isRecording, setIsRecording] = useState<boolean>(false);
  const [recordedLines, setRecordedLines] = useState<string[]>([]);
  const [graphData, setGraphData] = useState<{ rpm: number; speed: number; coolant: number; throttle: number }[]>([]);

  const simTimeRef = useRef(0);

  // Whenever cylinder count changes in settings, update DTCs
  useEffect(() => {
    setDtcs(getDTCsForVehicle(settings.specs?.cylinderCount ?? 1));
  }, [settings.specs?.cylinderCount]);

  // Handle connection state changes: When disconnected, reset all gauges cleanly to 0
  const handleSetIsConnected = (connected: boolean) => {
    setIsConnected(connected);
    if (!connected) {
      // Reset all values to 0 immediately
      setPids((prev) => prev.map((p) => ({ ...p, value: 0 })));
      setGraphData([]);
      if (isRecording) {
        setIsRecording(false);
      }
    }
  };

  // Engine Telemetry Simulation loop ONLY runs when isConnected === true
  useEffect(() => {
    if (!isConnected) {
      return;
    }

    const interval = setInterval(() => {
      simTimeRef.current += 0.12;
      const t = simTimeRef.current;

      const throttleInput = Math.max(0, Math.min(100, Math.round((Math.sin(t * 0.35) + 1) * 45)));
      const targetRpm = Math.round(1600 + throttleInput * 70 + Math.sin(t * 6) * 90);
      const targetSpeed = Math.round(Math.max(0, throttleInput * 1.18 + Math.sin(t * 0.2) * 5));
      const targetCoolant = Math.round(87 + Math.sin(t * 0.04) * 4);
      const targetBattery = parseFloat((14.1 + Math.sin(t * 2) * 0.12).toFixed(1));
      const targetLoad = Math.round(20 + throttleInput * 0.7);
      const targetMap = Math.round(42 + throttleInput * 0.85);

      setPids((prev) =>
        prev.map((p) => {
          switch (p.id) {
            case 'rpm': return { ...p, value: targetRpm };
            case 'speed': return { ...p, value: targetSpeed };
            case 'coolant': return { ...p, value: targetCoolant };
            case 'battery': return { ...p, value: targetBattery };
            case 'throttle': return { ...p, value: throttleInput };
            case 'load': return { ...p, value: targetLoad };
            case 'map': return { ...p, value: targetMap };
            default: return p;
          }
        })
      );

      setGraphData((prev) => {
        const next = [...prev, { rpm: targetRpm, speed: targetSpeed, coolant: targetCoolant, throttle: throttleInput }];
        return next.length > 50 ? next.slice(-50) : next;
      });

      if (isRecording) {
        const now = new Date().toISOString();
        setRecordedLines((prev) => [
          ...prev,
          `${now},${targetRpm},${targetSpeed},${targetCoolant},${targetBattery},${throttleInput}`,
        ]);
      }
    }, 120);

    return () => clearInterval(interval);
  }, [isConnected, isRecording]);

  const updateSettings = (newSettings: OBD2Settings) => {
    setSettings(newSettings);
    localStorage.setItem('motoscan_settings', JSON.stringify(newSettings));
  };

  const updatePIDConfig = (
    id: string,
    min: number,
    max: number,
    warnMin?: number,
    warnMax?: number,
    colorOptimal?: string,
    colorLow?: string,
    colorHigh?: string
  ) => {
    setPids((prev) => {
      const next = prev.map((p) =>
        p.id === id
          ? {
              ...p,
              min,
              max,
              warnMin,
              warnMax,
              colorOptimal: colorOptimal || p.colorOptimal,
              colorLow: colorLow || p.colorLow,
              colorHigh: colorHigh || p.colorHigh,
            }
          : p
      );
      localStorage.setItem('motoscan_pids', JSON.stringify(next));
      return next;
    });
  };

  const clearDtcs = () => {
    setDtcs([]);
  };

  const refreshDtcs = () => {
    setDtcs(getDTCsForVehicle(settings.specs?.cylinderCount ?? 1));
  };

  const toggleRecording = () => {
    if (!isConnected) return;
    if (isRecording) {
      const dateStr = new Date().toLocaleString();
      const filename = `Log_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.csv`;
      setLogs((prev) => [
        {
          name: filename,
          date: dateStr,
          size: `${Math.round(recordedLines.length * 0.08)} KB`,
          isFavorite: false,
          content: 'Timestamp,RPM,Speed,Coolant,Battery,Throttle\n' + recordedLines.join('\n'),
        },
        ...prev,
      ]);
      setRecordedLines([]);
      setIsRecording(false);
    } else {
      setIsRecording(true);
      setRecordedLines([]);
    }
  };

  const deleteLog = (name: string) => {
    setLogs((prev) => prev.filter((l) => l.name !== name));
  };

  const toggleFavoriteLog = (name: string) => {
    setLogs((prev) =>
      prev.map((l) => (l.name === name ? { ...l, isFavorite: !l.isFavorite } : l))
    );
  };

  return (
    <OBDContext.Provider
      value={{
        pids,
        dtcs,
        readiness,
        logs,
        settings,
        isConnected,
        isRecording,
        graphData,
        setIsConnected: handleSetIsConnected,
        updateSettings,
        toggleRecording,
        clearDtcs,
        refreshDtcs,
        deleteLog,
        toggleFavoriteLog,
        updatePIDConfig,
      }}
    >
      {children}
    </OBDContext.Provider>
  );
};

export function useOBDContext(): OBDContextType {
  const context = useContext(OBDContext);
  if (!context) {
    throw new Error('useOBDContext must be used within an OBDProvider');
  }
  return context;
}

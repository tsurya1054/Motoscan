import React from 'react';
import { useOBDContext } from '../OBDContext';
import { MotorcycleSpecs } from '../types';
import { Cpu, Wrench, CheckCircle } from 'lucide-react';

export const SettingsView: React.FC = () => {
  const { settings, updateSettings } = useOBDContext();

  const handleUpdateSpecs = (newSpecs: Partial<MotorcycleSpecs>) => {
    updateSettings({
      ...settings,
      specs: {
        ...settings.specs,
        ...newSpecs,
      },
    });
  };

  return (
    <div className="space-y-5 font-mono text-xs w-full">
      {/* SECTION 1: SPESIFIKASI LENGKAP MOTOR (MOTORCYCLE SPECIFICATIONS) */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl p-5 shadow-xl space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <Cpu className="w-4 h-4 text-cyan-400" />
            <h2 className="text-sm font-bold text-cyan-400 uppercase tracking-wider">
              SPESIFIKASI LENGKAP MESIN & MOTOR
            </h2>
          </div>
          <span className="text-[10px] text-slate-500">ENGINE SPECIFICATIONS</span>
        </div>

        {/* Manual Motorcycle Name Input */}
        <div>
          <label className="text-slate-400 block mb-1.5 font-bold">
            NAMA / MODEL MOTOR (MANUAL):
          </label>
          <input
            type="text"
            value={settings.profileName}
            onChange={(e) => updateSettings({ ...settings, profileName: e.target.value })}
            placeholder="Contoh: Honda ADV 150, Honda ADV 160 eSP+, Yamaha NMAX, dll."
            className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white font-extrabold focus:outline-none focus:border-cyan-400"
          />
          <span className="text-[10px] text-slate-500 mt-1 block">
            Masukkan nama motor sesuai preferensi Anda. Nama ini akan tampil pada diagnostik DTC dan log data.
          </span>
        </div>

        {/* Detailed Spec Inputs */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
          {/* CC / Displacement */}
          <div>
            <label className="text-slate-400 block mb-1">
              KAPASITAS MESIN (CC / ENGINE DISPLACEMENT) *
            </label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                value={settings.specs.engineDisplacementCc}
                onChange={(e) =>
                  handleUpdateSpecs({ engineDisplacementCc: parseInt(e.target.value, 10) || 0 })
                }
                className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white font-extrabold focus:outline-none focus:border-cyan-400"
              />
              <span className="text-cyan-400 font-bold px-2 py-2 bg-[#181E27] border border-slate-700 rounded-lg">
                cc
              </span>
            </div>
          </div>

          {/* Jumlah Silinder */}
          <div>
            <label className="text-slate-400 block mb-1">
              JUMLAH SILINDER (CYLINDER COUNT) *
            </label>
            <select
              value={settings.specs.cylinderCount}
              onChange={(e) =>
                handleUpdateSpecs({ cylinderCount: parseInt(e.target.value, 10) || 1 })
              }
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white font-extrabold focus:outline-none focus:border-cyan-400 cursor-pointer"
            >
              <option value={1}>1 Silinder (Single Cylinder - Standar Matic)</option>
              <option value={2}>2 Silinder (Twin Cylinder / Inline-2)</option>
              <option value={3}>3 Silinder (Triple)</option>
              <option value={4}>4 Silinder (Inline-4 / V4)</option>
            </select>
          </div>

          {/* Tipe Mesin */}
          <div>
            <label className="text-slate-400 block mb-1">TIPE MESIN (ENGINE TYPE)</label>
            <input
              type="text"
              value={settings.specs.engineType}
              onChange={(e) => handleUpdateSpecs({ engineType: e.target.value })}
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
              placeholder="Contoh: 4-Langkah, SOHC, eSP+"
            />
          </div>

          {/* Bore x Stroke */}
          <div>
            <label className="text-slate-400 block mb-1">BORE x STROKE (DIAMETER x LANGKAH)</label>
            <input
              type="text"
              value={settings.specs.boreStroke}
              onChange={(e) => handleUpdateSpecs({ boreStroke: e.target.value })}
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
              placeholder="Contoh: 57.3 x 57.9 mm"
            />
          </div>

          {/* Rasio Kompresi */}
          <div>
            <label className="text-slate-400 block mb-1">RASIO KOMPRESI (COMPRESSION RATIO)</label>
            <input
              type="text"
              value={settings.specs.compressionRatio}
              onChange={(e) => handleUpdateSpecs({ compressionRatio: e.target.value })}
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
              placeholder="Contoh: 10.6 : 1"
            />
          </div>

          {/* Kapasitas Tangki Bahan Bakar */}
          <div>
            <label className="text-slate-400 block mb-1">KAPASITAS TANGKI BBM (LITER)</label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                step="0.1"
                value={settings.specs.fuelTankCapacity}
                onChange={(e) =>
                  handleUpdateSpecs({ fuelTankCapacity: parseFloat(e.target.value) || 0 })
                }
                className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
              />
              <span className="text-cyan-400 font-bold px-2 py-2 bg-[#181E27] border border-slate-700 rounded-lg">
                Liter
              </span>
            </div>
          </div>

          {/* Tipe Transmisi */}
          <div>
            <label className="text-slate-400 block mb-1">TIPE TRANSMISI</label>
            <input
              type="text"
              value={settings.specs.transmissionType}
              onChange={(e) => handleUpdateSpecs({ transmissionType: e.target.value })}
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
              placeholder="Contoh: Otomatis, V-Matic (CVT)"
            />
          </div>

          {/* Tahun Motor */}
          <div>
            <label className="text-slate-400 block mb-1">TAHUN PEMBUATAN MOTOR</label>
            <input
              type="number"
              value={settings.specs.modelYear}
              onChange={(e) =>
                handleUpdateSpecs({ modelYear: parseInt(e.target.value, 10) || 2022 })
              }
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
            />
          </div>
        </div>
      </div>

      {/* SECTION 2: PROFIL KENDARAAN & KONEKSI OBD2 */}
      <div className="bg-[#11141A] border border-[#1E2530] rounded-xl p-5 shadow-xl space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <Wrench className="w-4 h-4 text-cyan-400" />
            <h2 className="text-sm font-bold text-cyan-400 uppercase tracking-wider">
              PROFIL KENDARAAN & KONEKSI PERANGKAT
            </h2>
          </div>
        </div>

        <div>
          <label className="text-slate-400 block mb-1">NAMA PROFIL KENDARAAN</label>
          <input
            type="text"
            value={settings.profileName}
            onChange={(e) => updateSettings({ ...settings, profileName: e.target.value })}
            className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
          />
        </div>

        <div>
          <label className="text-slate-400 block mb-1">PERANGKAT BLUETOOTH OBD2 TERHUBUNG</label>
          <input
            type="text"
            value={settings.pairedDevice}
            onChange={(e) => updateSettings({ ...settings, pairedDevice: e.target.value })}
            className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="text-slate-400 block mb-1">UPDATE RATE (Hz)</label>
            <select
              value={settings.updateRate}
              onChange={(e) =>
                updateSettings({ ...settings, updateRate: parseInt(e.target.value, 10) })
              }
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400 cursor-pointer"
            >
              <option value={5}>5 Hz (Standard)</option>
              <option value={10}>10 Hz (Default Responsif)</option>
              <option value={20}>20 Hz (Ultra High Speed)</option>
            </select>
          </div>

          <div>
            <label className="text-slate-400 block mb-1">SATUAN UKURAN</label>
            <select
              value={settings.units}
              onChange={(e) => updateSettings({ ...settings, units: e.target.value as any })}
              className="w-full bg-[#181E27] border border-slate-700 rounded-lg px-3 py-2 text-white focus:outline-none focus:border-cyan-400 cursor-pointer"
            >
              <option value="Metric">Metric (km/h, °C, kPa)</option>
              <option value="Imperial">Imperial (mph, °F, psi)</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  );
};

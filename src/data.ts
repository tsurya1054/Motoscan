import { PID, DTC, ReadinessItem, LogFile, OBD2Settings } from './types';

export const INITIAL_PIDS: PID[] = [
  {
    id: 'rpm',
    name: 'Engine RPM',
    shortName: 'RPM',
    obdPid: '010C',
    value: 0,
    unit: 'rpm',
    category: 'ENGINE',
    min: 0,
    max: 12000,
    warnMin: 1000,
    warnMax: 9500,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'coolant',
    name: 'Coolant Temp',
    shortName: 'Coolant',
    obdPid: '0105',
    value: 0,
    unit: '°C',
    category: 'ENGINE',
    min: 0,
    max: 150,
    warnMax: 115,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'speed',
    name: 'Vehicle Speed',
    shortName: 'Speed',
    obdPid: '010D',
    value: 0,
    unit: 'km/h',
    category: 'ENGINE',
    min: 0,
    max: 200,
    warnMax: 150,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'battery',
    name: 'Battery Voltage',
    shortName: 'Battery',
    obdPid: 'ATRV',
    value: 0,
    unit: 'V',
    category: 'OTHER',
    min: 9,
    max: 16,
    warnMin: 11.5,
    warnMax: 15.2,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'throttle',
    name: 'Throttle Position',
    shortName: 'Throttle',
    obdPid: '0111',
    value: 0,
    unit: '%',
    category: 'SENSOR',
    min: 0,
    max: 100,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'load',
    name: 'Engine Load',
    shortName: 'Load',
    obdPid: '0104',
    value: 0,
    unit: '%',
    category: 'ENGINE',
    min: 0,
    max: 100,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'iat',
    name: 'Intake Air Temp',
    shortName: 'IAT',
    obdPid: '010F',
    value: 0,
    unit: '°C',
    category: 'SENSOR',
    min: 0,
    max: 100,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
  {
    id: 'map',
    name: 'Manifold Pressure',
    shortName: 'MAP',
    obdPid: '010B',
    value: 0,
    unit: 'kPa',
    category: 'SENSOR',
    min: 0,
    max: 255,
    colorOptimal: '#00E5FF',
    colorLow: '#FFD600',
    colorHigh: '#FF1744',
  },
];

// Helper to generate DTCs matching the vehicle specifications (cylinder count, etc.)
export const getDTCsForVehicle = (cylinderCount: number): DTC[] => {
  const result: DTC[] = [
    {
      code: 'P0171',
      description: 'System Too Lean (Campuran Bahan Bakar Terlalu Kurus)',
      status: 'ERROR',
      category: 'Fuel and Air Metering',
    },
    {
      code: 'P0118',
      description: 'Engine Coolant Temperature (ECT) Sensor Circuit High',
      status: 'ERROR',
      category: 'Cooling System & Sensors',
    },
  ];

  // Cylinder misfire errors strictly matched to cylinder count
  if (cylinderCount === 1) {
    result.push({
      code: 'P0301',
      description: 'Cylinder 1 Misfire Detected (Pengapian / Busi Silinder 1)',
      status: 'ERROR',
      category: 'Ignition System (Single Cylinder)',
    });
  } else {
    // 2 or more cylinders
    result.push({
      code: 'P0301',
      description: 'Cylinder 1 Misfire Detected (Pengapian Silinder 1)',
      status: 'ERROR',
      category: 'Ignition System',
    });
    result.push({
      code: 'P0302',
      description: 'Cylinder 2 Misfire Detected (Pengapian Silinder 2)',
      status: 'ERROR',
      category: 'Ignition System',
    });

    if (cylinderCount >= 3) {
      result.push({
        code: 'P0303',
        description: 'Cylinder 3 Misfire Detected (Pengapian Silinder 3)',
        status: 'ERROR',
        category: 'Ignition System',
      });
    }

    if (cylinderCount >= 4) {
      result.push({
        code: 'P0304',
        description: 'Cylinder 4 Misfire Detected (Pengapian Silinder 4)',
        status: 'ERROR',
        category: 'Ignition System',
      });
    }
  }

  result.push({
    code: 'P0122',
    description: 'Throttle Position Sensor (TPS) Circuit Low Input',
    status: 'PENDING',
    category: 'Throttle & Air Intake',
  });

  return result;
};

export const INITIAL_READINESS: ReadinessItem[] = [
  { id: 'misfire', name: 'Misfire Monitor', nameIndo: 'Pemantau Misfire', status: 'READY' },
  { id: 'fuelSystem', name: 'Fuel System Monitor', nameIndo: 'Sistem Bahan Bakar', status: 'READY' },
  { id: 'components', name: 'Comprehensive Components', nameIndo: 'Komponen Komprehensif', status: 'READY' },
  { id: 'catalyst', name: 'Catalyst Monitor', nameIndo: 'Katalisator', status: 'READY' },
  { id: 'heatedCatalyst', name: 'Heated Catalyst', nameIndo: 'Pemanas Katalis', status: 'NOT_READY' },
  { id: 'evapSystem', name: 'EVAP System', nameIndo: 'Sistem Evaporasi', status: 'READY' },
  { id: 'secondaryAir', name: 'Secondary Air System', nameIndo: 'Sistem Udara Sekunder', status: 'NOT_READY' },
  { id: 'acRefrigerant', name: 'A/C Refrigerant', nameIndo: 'Refrigeran A/C', status: 'READY' },
  { id: 'o2Sensor', name: 'Oxygen Sensor', nameIndo: 'Sensor Oksigen', status: 'READY' },
  { id: 'o2Heater', name: 'O2 Sensor Heater', nameIndo: 'Pemanas Sensor O2', status: 'READY' },
  { id: 'egrSystem', name: 'EGR System', nameIndo: 'Sistem EGR', status: 'READY' },
];

export const INITIAL_LOGS: LogFile[] = [
  {
    name: 'Log_2024-05-26_19-45-12.csv',
    date: '26/05/2024 19:45',
    size: '1.2 MB',
    isFavorite: true,
    content: 'Timestamp,RPM,Speed,CoolantTemp,Voltage,Throttle,EngineLoad,MAP\n',
  },
  {
    name: 'Log_2024-05-26_18-12-08.csv',
    date: '26/05/2024 18:12',
    size: '850 KB',
    isFavorite: false,
    content: 'Timestamp,RPM,Speed,CoolantTemp,Voltage,Throttle,EngineLoad,MAP\n',
  },
];

export const INITIAL_SETTINGS: OBD2Settings = {
  connectionType: 'Bluetooth',
  pairedDevice: 'ELM327 v2.1 (ADV 150)',
  autoConnect: false,
  updateRate: 10,
  units: 'Metric',
  language: 'Indonesian',
  profileName: 'Honda ADV 150 (Bore Up 180cc)',
  specs: {
    engineDisplacementCc: 180,
    cylinderCount: 1, // Single cylinder default for Honda ADV
    engineType: '4-Langkah, SOHC, eSP+, Berpendingin Cairan',
    boreStroke: '63.0 x 57.9 mm (Bore-Up)',
    compressionRatio: '11.8 : 1',
    fuelTankCapacity: 8.0,
    transmissionType: 'Otomatis, V-Matic (CVT Racing Pulley)',
    modelYear: 2022,
  },
};

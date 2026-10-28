import React, { Profiler } from 'react';

interface PerformanceProfilerProps {
  id: string;
  children: React.ReactNode;
  onRender?: (
    id: string,
    phase: "mount" | "update",
    actualDuration: number,
    baseDuration: number,
    startTime: number,
    commitTime: number
  ) => void;
}

export const PerformanceProfiler: React.FC<PerformanceProfilerProps> = ({ id, children, onRender }) => {
  if (import.meta.env.VITE_ENABLE_REACT_PROFILER !== 'true') {
    return <>{children}</>;
  }

  const handleRender: React.ProfilerOnRenderCallback = (
    id,
    phase,
    actualDuration,
    baseDuration,
    startTime,
    commitTime
  ) => {
    if (onRender) {
      onRender(id, phase as "mount" | "update", actualDuration, baseDuration, startTime, commitTime);
    } else if (import.meta.env.DEV && actualDuration > 16) {
      console.warn(`[React Profiler] Slow render detected in "${id}" (${phase}): ${actualDuration.toFixed(2)}ms (Base: ${baseDuration.toFixed(2)}ms)`);
    }
  };

  return (
    <Profiler id={id} onRender={handleRender}>
      {children}
    </Profiler>
  );
};

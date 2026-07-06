"use client";

import { type ReactNode, useEffect, useMemo, useRef, useState } from "react";

interface MinimalDatePickerProps {
  value: string;
  minDate: string;
  onChange: (value: string) => void;
  label?: ReactNode;
  placeholder?: string;
}

const WEEKDAY_LABELS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];

function parseDateString(dateValue: string): Date {
  return new Date(`${dateValue}T00:00:00`);
}

function formatDateString(dateValue: Date): string {
  const year = dateValue.getFullYear();
  const month = String(dateValue.getMonth() + 1).padStart(2, "0");
  const day = String(dateValue.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function getMonthLabel(dateValue: Date): string {
  return dateValue.toLocaleDateString("en-US", {
    month: "long",
    year: "numeric",
  });
}

function getMonthStart(dateValue: Date): Date {
  return new Date(dateValue.getFullYear(), dateValue.getMonth(), 1);
}

function isSameMonth(leftDate: Date, rightDate: Date): boolean {
  return (
    leftDate.getFullYear() === rightDate.getFullYear() &&
    leftDate.getMonth() === rightDate.getMonth()
  );
}

function buildCalendarDays(visibleMonth: Date): Date[] {
  const monthStart = getMonthStart(visibleMonth);
  const calendarStart = new Date(monthStart);
  calendarStart.setDate(monthStart.getDate() - monthStart.getDay());

  return Array.from({ length: 42 }, (_unusedValue, index) => {
    const nextDate = new Date(calendarStart);
    nextDate.setDate(calendarStart.getDate() + index);
    return nextDate;
  });
}

export default function MinimalDatePicker({
  value,
  minDate,
  onChange,
  label,
  placeholder = "YYYY-MM-DD",
}: MinimalDatePickerProps) {
  const selectedDate = value ? parseDateString(value) : null;
  const minDateObject = parseDateString(minDate);
  const [isOpen, setIsOpen] = useState(false);
  const [visibleMonth, setVisibleMonth] = useState<Date>(
    selectedDate ?? minDateObject
  );
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const handlePointerDown = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, [isOpen]);

  const calendarDays = useMemo(
    () => buildCalendarDays(visibleMonth),
    [visibleMonth]
  );

  const canGoToPreviousMonth = useMemo(() => {
    const previousMonth = new Date(
      visibleMonth.getFullYear(),
      visibleMonth.getMonth() - 1,
      1
    );
    const minMonth = new Date(
      minDateObject.getFullYear(),
      minDateObject.getMonth(),
      1
    );
    return previousMonth >= minMonth;
  }, [minDateObject, visibleMonth]);

  const handleSelectDate = (dateValue: Date) => {
    const nextValue = formatDateString(dateValue);
    if (nextValue < minDate) {
      return;
    }
    onChange(nextValue);
    setIsOpen(false);
  };

  return (
    <div ref={containerRef} className="relative">
      {label ? (
        <label className="mb-2 block text-sm text-muted">{label}</label>
      ) : null}

      <button
        type="button"
        onClick={() => {
          if (!isOpen) {
            setVisibleMonth(selectedDate ?? minDateObject);
          }
          setIsOpen((previousValue) => !previousValue);
        }}
        className="flex w-full items-center justify-between border border-border bg-white px-3 py-3 text-sm transition-colors hover:border-foreground"
      >
        <span className={value ? "text-foreground" : "text-muted"}>
          {value || placeholder}
        </span>
        <svg
          width="18"
          height="18"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="text-muted"
        >
          <path d="M8 2v4" />
          <path d="M16 2v4" />
          <rect width="18" height="18" x="3" y="4" rx="2" />
          <path d="M3 10h18" />
        </svg>
      </button>

      {isOpen ? (
        <div className="absolute left-0 top-full z-20 mt-3 w-[320px] border border-border bg-white p-4 shadow-[0_24px_48px_rgba(15,23,42,0.12)]">
          <div className="mb-4 flex items-center justify-between">
            <button
              type="button"
              onClick={() =>
                setVisibleMonth(
                  (previousMonth) =>
                    new Date(
                      previousMonth.getFullYear(),
                      previousMonth.getMonth() - 1,
                      1
                    )
                )
              }
              disabled={!canGoToPreviousMonth}
              className="inline-flex h-9 w-9 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground disabled:cursor-not-allowed disabled:opacity-40"
              aria-label="Previous month"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="m15 18-6-6 6-6" />
              </svg>
            </button>
            <p className="text-sm font-medium text-foreground">
              {getMonthLabel(visibleMonth)}
            </p>
            <button
              type="button"
              onClick={() =>
                setVisibleMonth(
                  (previousMonth) =>
                    new Date(
                      previousMonth.getFullYear(),
                      previousMonth.getMonth() + 1,
                      1
                    )
                )
              }
              className="inline-flex h-9 w-9 items-center justify-center border border-border text-foreground transition-colors hover:border-foreground"
              aria-label="Next month"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="m9 18 6-6-6-6" />
              </svg>
            </button>
          </div>

          <div className="mb-2 grid grid-cols-7 gap-1">
            {WEEKDAY_LABELS.map((weekdayLabel) => (
              <div
                key={weekdayLabel}
                className="flex h-9 items-center justify-center text-xs uppercase tracking-[1.5px] text-muted"
              >
                {weekdayLabel}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-1">
            {calendarDays.map((calendarDay) => {
              const calendarDayValue = formatDateString(calendarDay);
              const isDisabled = calendarDayValue < minDate;
              const isCurrentMonth = isSameMonth(calendarDay, visibleMonth);
              const isSelected = calendarDayValue === value;

              return (
                <button
                  key={calendarDayValue}
                  type="button"
                  onClick={() => handleSelectDate(calendarDay)}
                  disabled={isDisabled}
                  className={`flex h-10 items-center justify-center text-sm transition-colors ${
                    isSelected
                      ? "bg-foreground text-white"
                      : isCurrentMonth
                        ? "text-foreground hover:bg-surface"
                        : "text-muted hover:bg-surface"
                  } ${isDisabled ? "cursor-not-allowed opacity-30 hover:bg-transparent" : ""}`}
                >
                  {calendarDay.getDate()}
                </button>
              );
            })}
          </div>

          <div className="mt-4 flex items-center justify-between border-t border-border pt-4">
            <p className="text-xs uppercase tracking-[1.5px] text-muted">
              Earliest: {minDate}
            </p>
            <button
              type="button"
              onClick={() => handleSelectDate(minDateObject)}
              className="text-sm text-foreground transition-colors hover:text-muted"
            >
              Choose Today
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

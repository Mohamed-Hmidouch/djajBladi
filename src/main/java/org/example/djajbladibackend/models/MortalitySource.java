package org.example.djajbladibackend.models;

/**
 * Source of a daily mortality record.
 * WORKER_REPORT: recorded by an ouvrier during daily rounds.
 * VETERINARIAN_EXAMINATION: automatically created when a veterinarian records mortality in a health record.
 */
public enum MortalitySource {
    WORKER_REPORT,
    VETERINARIAN_EXAMINATION
}

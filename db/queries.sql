-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT DISTINCT
    t.instrument_id,
    t.trade_date,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'
ORDER BY t.trade_date DESC, t.instrument_id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup (5 stages)
--                EXECUTION -> CONFIRMATION -> SETTLEMENT -> RECON_BREAK -> RESOLUTION
--
-- Each recursive step uses a LATERAL join so it naturally stops for a trade
-- once no row exists for the next stage — e.g. a cleanly-matched trade with
-- no recon_breaks row simply terminates at SETTLED (step 3), it does not
-- error or force a synthetic RECON_BREAK row.
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- anchor: every trade in its execution state
    SELECT
        t.id           AS trade_id,
        t.trade_ref,
        1              AS step,
        'EXECUTED'     AS state,
        t.created_at   AS at_ts,
        NULL::text     AS detail
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- recursive: each subsequent state derived from the previous step
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.step + 1,
        CASE tl.step
            WHEN 1 THEN 'CONFIRMED'
            WHEN 2 THEN 'SETTLED'
            WHEN 3 THEN 'RECON_BREAK'
            WHEN 4 THEN 'RESOLVED'
        END                                          AS state,
        next_event.at_ts,
        next_event.detail
    FROM trade_lifecycle tl
    JOIN LATERAL (
        -- steps 1->2 and 2->3: reuse the settlement row (CONFIRMED, then SETTLED)
        SELECT s.settlement_date::timestamp AS at_ts, s.status AS detail
        FROM settlements s
        WHERE s.trade_id = tl.trade_id AND tl.step IN (1, 2)

        UNION ALL

        -- step 3->4: an open (or resolved) recon break was raised against this trade
        SELECT rb.detected_at, rb.discrepancy_type
        FROM recon_breaks rb
        WHERE rb.trade_id = tl.trade_id AND tl.step = 3

        UNION ALL

        -- step 4->5: the break was resolved; no row here leaves the trade at RECON_BREAK
        SELECT rb.resolved_at, rb.resolution_note
        FROM recon_breaks rb
        WHERE rb.trade_id = tl.trade_id AND tl.step = 4 AND rb.resolved_at IS NOT NULL
    ) AS next_event ON TRUE
    WHERE tl.step < 5
)
SELECT * FROM trade_lifecycle
ORDER BY trade_id, step;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;

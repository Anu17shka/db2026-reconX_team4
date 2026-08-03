// TICKET-ADV118 — useInfiniteScroll: invokes loadMore() when sentinel is visible.
import { useRef, useEffect } from 'react';

export function useInfiniteScroll(loadMore) {
  const sentinelRef = useRef(null);

  useEffect(() => {
    const node = sentinelRef.current;
    if (!node) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting) loadMore();
    });

    observer.observe(node);
    return () => observer.disconnect();
  }, [loadMore]);

  return sentinelRef;
}

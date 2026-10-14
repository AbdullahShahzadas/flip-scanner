<?php
declare(strict_types=1);

namespace FlipScanner\Api;

use FlipScanner\Services\EbayService;

/**
 * GET /api/comparables?q=...&limit=10
 * Returns active eBay listings for a search query.
 */
class ComparableController
{
    public function search(): void
    {
        header('Content-Type: application/json');

        $query = trim((string) ($_GET['q'] ?? ''));
        if ($query === '') {
            http_response_code(400);
            echo json_encode(['error' => 'BAD_REQUEST', 'message' => 'Missing q parameter.']);
            return;
        }

        $limit = max(1, min(20, (int) ($_GET['limit'] ?? 10)));

        try {
            $ebay = new EbayService();
            $items = $ebay->searchActiveListings($query, $limit);
            $range = EbayService::priceRange(array_column($items, 'price'));

            http_response_code(200);
            echo json_encode([
                'query' => $query,
                'count' => count($items),
                'price_low' => $range['low'],
                'price_typical' => $range['typical'],
                'price_high' => $range['high'],
                'items' => $items,
            ]);
        } catch (\RuntimeException $e) {
            http_response_code(503);
            echo json_encode(['error' => 'EBAY_UNAVAILABLE', 'message' => $e->getMessage()]);
        }
    }
}

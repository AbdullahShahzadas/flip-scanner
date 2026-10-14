<?php
declare(strict_types=1);

namespace FlipScanner\Api;

use FlipScanner\Config;
use FlipScanner\Database;
use FlipScanner\Services\AiVisionService;
use FlipScanner\Services\EbayService;

/**
 * POST /api/scan
 * Accepts up to 3 photos, runs AI identification, fetches eBay
 * comparables, and returns a full scan result.
 *
 * Multipart fields: photos[] (1-3 images), purchase_price (optional float).
 */
class ScanController
{
    public function scan(): void
    {
        header('Content-Type: application/json');

        try {
            $this->validateRequest();
            $imagePaths = $this->storeUploads();

            try {
                $ai = new AiVisionService();
                // Identify from the first (primary) photo.
                $identified = $ai->identify($imagePaths[0]);
            } catch (\RuntimeException $e) {
                $this->respond(503, ['error' => 'AI_UNAVAILABLE', 'message' => $e->getMessage()]);
                return;
            }

            // Fetch eBay comparables for the identified item.
            $comparables = [];
            $range = ['low' => 0.0, 'typical' => 0.0, 'high' => 0.0];
            try {
                $ebay = new EbayService();
                $query = trim($identified['brand'] . ' ' . $identified['item'] . ' ' . $identified['model']);
                $comparables = $ebay->searchActiveListings($query, 12);
                $prices = array_column($comparables, 'price');
                $range = EbayService::priceRange($prices);
            } catch (\RuntimeException $e) {
                // Comparables are best-effort; the scan still succeeds.
                $comparables = [];
            }

            $purchasePrice = isset($_POST['purchase_price'])
                ? (float) $_POST['purchase_price']
                : 0.0;

            $profit = $range['typical'] - $purchasePrice;
            $verdict = $this->verdict($profit, $purchasePrice, $range['typical']);

            $scanId = $this->persistScan($identified, $range, $comparables, $purchasePrice, $imagePaths);

            $this->respond(200, [
                'scan_id' => $scanId,
                'item' => $identified['item'],
                'brand' => $identified['brand'],
                'model' => $identified['model'],
                'condition' => $identified['condition'],
                'confidence' => $identified['confidence'],
                'category' => $identified['category'],
                'price_low' => $range['low'],
                'price_typical' => $range['typical'],
                'price_high' => $range['high'],
                'comparables' => array_slice($comparables, 0, 6),
                'purchase_price' => $purchasePrice,
                'estimated_profit' => round($profit, 2),
                'verdict' => $verdict,
                'photos' => count($imagePaths),
            ]);
        } catch (\InvalidArgumentException $e) {
            $this->respond(400, ['error' => 'BAD_REQUEST', 'message' => $e->getMessage()]);
        } catch (\Throwable $e) {
            error_log('Scan failed: ' . $e->getMessage());
            $this->respond(500, ['error' => 'SCAN_FAILED', 'message' => 'Something went wrong.']);
        }
    }

    private function validateRequest(): void
    {
        if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') {
            throw new \InvalidArgumentException('Use POST with multipart photos[].');
        }
        if (empty($_FILES['photos'])) {
            throw new \InvalidArgumentException('No photos uploaded (field: photos[]).');
        }
    }

    /**
     * @return string[] Local paths of stored images.
     */
    private function storeUploads(): array
    {
        $files = $_FILES['photos'];
        $count = is_array($files['name']) ? count($files['name']) : 1;
        if ($count < 1 || $count > 3) {
            throw new \InvalidArgumentException('Upload 1 to 3 photos.');
        }

        $uploadDir = Config::get('UPLOAD_DIR', sys_get_temp_dir() . '/flipscanner');
        if (!is_dir($uploadDir)) {
            mkdir($uploadDir, 0755, true);
        }

        $paths = [];
        for ($i = 0; $i < $count; $i++) {
            $name = is_array($files['name']) ? $files['name'][$i] : $files['name'];
            $tmp = is_array($files['tmp_name']) ? $files['tmp_name'][$i] : $files['tmp_name'];
            $error = is_array($files['error']) ? $files['error'][$i] : $files['error'];
            $size = is_array($files['size']) ? $files['size'][$i] : $files['size'];

            if ($error !== UPLOAD_ERR_OK) {
                throw new \InvalidArgumentException("Upload failed for photo " . ($i + 1) . ".");
            }
            if ($size > 10 * 1024 * 1024) {
                throw new \InvalidArgumentException('Each photo must be under 10 MB.');
            }

            $ext = strtolower(pathinfo((string) $name, PATHINFO_EXTENSION));
            if (!in_array($ext, ['jpg', 'jpeg', 'png', 'webp'], true)) {
                throw new \InvalidArgumentException('Photos must be JPG, PNG, or WebP.');
            }

            $dest = $uploadDir . '/' . bin2hex(random_bytes(16)) . '.' . $ext;
            if (!move_uploaded_file($tmp, $dest)) {
                throw new \RuntimeException('Could not store uploaded photo.');
            }
            $paths[] = $dest;
        }

        return $paths;
    }

    private function verdict(float $profit, float $purchasePrice, float $typical): string
    {
        if ($purchasePrice <= 0 || $typical <= 0) {
            return 'UNKNOWN';
        }
        $margin = $profit / $purchasePrice;
        if ($margin >= 0.5) {
            return 'WORTH_IT';
        }
        if ($margin >= 0.2) {
            return 'MAYBE';
        }
        return 'SKIP';
    }

    private function persistScan(array $identified, array $range, array $comparables, float $purchasePrice, array $imagePaths): int
    {
        $db = Database::connection();
        $stmt = $db->prepare(
            'INSERT INTO scans (item_name, brand, model, `condition`, confidence, category, '
            . 'price_low, price_typical, price_high, purchase_price, photo_count, created_at) '
            . 'VALUES (:item, :brand, :model, :cond, :conf, :cat, :low, :typ, :high, :pp, :pc, NOW())'
        );
        $stmt->execute([
            ':item' => $identified['item'],
            ':brand' => $identified['brand'],
            ':model' => $identified['model'],
            ':cond' => $identified['condition'],
            ':conf' => $identified['confidence'],
            ':cat' => $identified['category'],
            ':low' => $range['low'],
            ':typ' => $range['typical'],
            ':high' => $range['high'],
            ':pp' => $purchasePrice,
            ':pc' => count($imagePaths),
        ]);

        return (int) $db->lastInsertId();
    }

    private function respond(int $code, array $body): void
    {
        http_response_code($code);
        echo json_encode($body);
    }
}

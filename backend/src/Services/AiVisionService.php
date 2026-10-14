<?php
declare(strict_types=1);

namespace FlipScanner\Services;

use FlipScanner\Config;

/**
 * AI vision service for item identification.
 * Uses OpenAI Vision API. Swap the implementation here to use
 * Google Vision, Claude, or another provider without touching controllers.
 */
class AiVisionService
{
    private string $apiKey;
    private string $model;

    public function __construct()
    {
        $this->apiKey = Config::get('OPENAI_API_KEY', '');
        $this->model = Config::get('OPENAI_VISION_MODEL', 'gpt-4o-mini');
    }

    /**
     * Identify an item from a photo.
     *
     * @param string $imagePath Local path to the uploaded image.
     * @return array{item: string, brand: string, model: string, condition: string, confidence: float, category: string}
     * @throws \RuntimeException on API failure.
     */
    public function identify(string $imagePath): array
    {
        if ($this->apiKey === '') {
            throw new \RuntimeException('OPENAI_API_KEY is not configured.');
        }

        $imageData = base64_encode((string) file_get_contents($imagePath));
        $mime = mime_content_type($imagePath) ?: 'image/jpeg';

        $payload = [
            'model' => $this->model,
            'messages' => [
                [
                    'role' => 'system',
                    'content' => 'You identify secondhand/resale items from photos. '
                        . 'Respond ONLY with valid JSON: '
                        . '{"item":"short name","brand":"brand or Unknown","model":"model or Unknown",'
                        . '"condition":"New|Like New|Good|Fair|Poor","confidence":0.0-1.0,'
                        . '"category":"e.g. Sneakers, Electronics, Fashion"}',
                ],
                [
                    'role' => 'user',
                    'content' => [
                        ['type' => 'text', 'text' => 'Identify this item for resale.'],
                        [
                            'type' => 'image_url',
                            'image_url' => ['url' => "data:{$mime};base64,{$imageData}"],
                        ],
                    ],
                ],
            ],
            'max_tokens' => 300,
        ];

        $ch = curl_init('https://api.openai.com/v1/chat/completions');
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_HTTPHEADER => [
                'Content-Type: application/json',
                'Authorization: Bearer ' . $this->apiKey,
            ],
            CURLOPT_POSTFIELDS => json_encode($payload),
            CURLOPT_TIMEOUT => 30,
        ]);

        $response = curl_exec($ch);
        $httpCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $curlError = curl_error($ch);
        curl_close($ch);

        if ($response === false || $httpCode !== 200) {
            throw new \RuntimeException(
                'AI identification failed (HTTP ' . $httpCode . '): ' . $curlError
            );
        }

        $data = json_decode((string) $response, true);
        $content = $data['choices'][0]['message']['content'] ?? '';

        // Strip markdown code fences if present.
        $content = trim((string) preg_replace('/^```json\s*|\s*```$/', '', trim($content)));

        $result = json_decode($content, true);
        if (!is_array($result) || !isset($result['item'])) {
            throw new \RuntimeException('AI returned an unexpected response format.');
        }

        return [
            'item' => (string) ($result['item'] ?? 'Unknown item'),
            'brand' => (string) ($result['brand'] ?? 'Unknown'),
            'model' => (string) ($result['model'] ?? 'Unknown'),
            'condition' => (string) ($result['condition'] ?? 'Good'),
            'confidence' => (float) ($result['confidence'] ?? 0.5),
            'category' => (string) ($result['category'] ?? 'Other'),
        ];
    }
}

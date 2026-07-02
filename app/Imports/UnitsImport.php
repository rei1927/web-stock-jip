<?php

namespace App\Imports;

use App\Models\Unit;
use App\Models\Cluster;
use Illuminate\Support\Collection;
use Maatwebsite\Excel\Concerns\ToCollection;
use Maatwebsite\Excel\Concerns\WithStartRow;

class UnitsImport implements ToCollection, WithStartRow
{
    /**
     * @return int
     */
    public function startRow(): int
    {
        return 2; // Data starts at row 2 (row 1 is header)
    }

    /**
    * @param Collection $rows
    */
    public function collection(Collection $rows)
    {
        foreach ($rows as $row) {
            // Check if essential data is missing
            if (!isset($row[1]) || !isset($row[2])) {
                continue;
            }

            // Find or create the cluster by name (Column 1)
            $clusterName = trim($row[1]);
            $cluster = Cluster::firstOrCreate(['name' => $clusterName]);

            // Construct details JSON array
            $details = [
                'type' => isset($row[3]) ? trim($row[3]) : null,
                'landArea' => isset($row[4]) ? (int) $row[4] : null,
                'buildingArea' => isset($row[5]) ? (int) $row[5] : null,
                'cash_1x' => isset($row[6]) ? (float) preg_replace('/[^0-9]/', '', $row[6]) : null,
                'cash_2x' => isset($row[7]) ? (float) preg_replace('/[^0-9]/', '', $row[7]) : null,
                'cash_3x' => isset($row[8]) ? (float) preg_replace('/[^0-9]/', '', $row[8]) : null,
                'kpr_1x' => isset($row[9]) ? (float) preg_replace('/[^0-9]/', '', $row[9]) : null,
                'kpr_2x' => isset($row[10]) ? (float) preg_replace('/[^0-9]/', '', $row[10]) : null,
                'kpr_3x' => isset($row[11]) ? (float) preg_replace('/[^0-9]/', '', $row[11]) : null,
                'description' => isset($row[14]) ? trim($row[14]) : null,
            ];

            $sellingPrice = isset($row[12]) ? (float) preg_replace('/[^0-9]/', '', $row[12]) : 0;
            $status = isset($row[13]) ? strtolower(trim($row[13])) : 'available';

            if (!in_array($status, ['available', 'reserved', 'sold', 'hold', 'request_booking'])) {
                $status = 'available';
            }

            // Create or update the unit by block name
            Unit::updateOrCreate(
                [
                    'cluster_id' => $cluster->id,
                    'block' => trim($row[2]),
                ],
                [
                    'name' => trim($row[2]),
                    'selling_price' => $sellingPrice,
                    'status' => $status,
                    'unit_details' => $details,
                ]
            );
        }
    }
}

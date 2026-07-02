<?php

namespace App\Exports;

use Maatwebsite\Excel\Concerns\FromArray;
use Maatwebsite\Excel\Concerns\WithHeadings;

class UnitsTemplateExport implements FromArray, WithHeadings
{
    /**
     * @return array
     */
    public function array(): array
    {
        // Return some empty rows or sample data so the user knows how to fill it
        return [
            [
                '1', 'Imperial Jade', 'B-01', 'RUBY', '60', '57', '627492000', '630394800', '633297600', '628943400', '631846200', '636200400', '837264087', 'available', 'Dekat taman'
            ],
            [
                '2', 'Imperial Jade', 'B-02', 'RUBY', '60', '57', '627492000', '630394800', '633297600', '628943400', '631846200', '636200400', '837264087', 'sold', ''
            ]
        ];
    }

    public function headings(): array
    {
        return [
            'NO',
            'CLUSTER',
            'BLOK',
            'TYPE',
            'LUAS TANAH',
            'LUAS BANGUNAN',
            'CASH 1X',
            'CASH 2X',
            'CASH 3X',
            'KPR 1X',
            'KPR 2X',
            'KPR 3X',
            'HARGA NORMAL',
            'STATUS',
            'KETERANGAN'
        ];
    }
}

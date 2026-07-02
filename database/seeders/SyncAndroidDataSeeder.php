<?php

namespace Database\Seeders;

use App\Models\Cluster;
use App\Models\Unit;
use Illuminate\Database\Seeder;

class SyncAndroidDataSeeder extends Seeder
{
    public function run(): void
    {
        $clustersData = ['Emerald', 'Imperial Garden', 'Royal Residence', 'Jasmine Spring'];
        $clusters = [];

        foreach ($clustersData as $clusterName) {
            $clusters[$clusterName] = Cluster::firstOrCreate([
                'name' => $clusterName
            ], [
                'description' => 'Pre-populated cluster from Android data'
            ]);
        }

        $unitsData = [
            [
                'cluster_name' => 'Emerald', 'block' => 'Blok B-01', 'name' => 'Tipe 70/120',
                'selling_price' => 1500000000, 'status' => 'available',
                'details' => ['buildingArea' => 70, 'landArea' => 120, 'bedrooms' => 3, 'bathrooms' => 2, 'notes' => 'Posisi strategis di hook, dekat taman bermain utama.']
            ],
            [
                'cluster_name' => 'Emerald', 'block' => 'Blok B-02', 'name' => 'Tipe 70/120',
                'selling_price' => 1500000000, 'status' => 'sold',
                'details' => ['buildingArea' => 70, 'landArea' => 120, 'bedrooms' => 3, 'bathrooms' => 2, 'notes' => 'Dekat dengan gerbang cluster utama.']
            ],
            [
                'cluster_name' => 'Emerald', 'block' => 'Blok B-05', 'name' => 'Tipe 60/100',
                'selling_price' => 1250000000, 'status' => 'available',
                'details' => ['buildingArea' => 60, 'landArea' => 100, 'bedrooms' => 3, 'bathrooms' => 2, 'notes' => 'Sistem ventilasi optimal, pencahayaan alami sangat baik.']
            ],
            [
                'cluster_name' => 'Imperial Garden', 'block' => 'Blok A-10', 'name' => 'Tipe 45/90',
                'selling_price' => 850000000, 'status' => 'hold',
                'details' => ['buildingArea' => 45, 'landArea' => 90, 'bedrooms' => 2, 'bathrooms' => 1, 'notes' => 'Rumah tumbuh minimalis modern dengan sisa lahan belakang luas.']
            ],
            [
                'cluster_name' => 'Imperial Garden', 'block' => 'Blok A-11', 'name' => 'Tipe 45/90',
                'selling_price' => 850000000, 'status' => 'sold',
                'details' => ['buildingArea' => 45, 'landArea' => 90, 'bedrooms' => 2, 'bathrooms' => 1, 'notes' => 'Carport muat 2 mobil kompak.']
            ],
            [
                'cluster_name' => 'Royal Residence', 'block' => 'Blok C-03', 'name' => 'Tipe 120/180',
                'selling_price' => 2400000000, 'status' => 'booked',
                'details' => ['buildingArea' => 120, 'landArea' => 180, 'bedrooms' => 4, 'bathrooms' => 3, 'notes' => 'Cluster teratas dengan Club House eksklusif, smart home ready.']
            ],
            [
                'cluster_name' => 'Royal Residence', 'block' => 'Blok C-04', 'name' => 'Tipe 120/180',
                'selling_price' => 2400000000, 'status' => 'sold',
                'details' => ['buildingArea' => 120, 'landArea' => 180, 'bedrooms' => 4, 'bathrooms' => 3, 'notes' => 'Sudah renovasi kanopi carport modern premium.']
            ],
            [
                'cluster_name' => 'Jasmine Spring', 'block' => 'Blok D-08', 'name' => 'Tipe 36/72',
                'selling_price' => 600000000, 'status' => 'available',
                'details' => ['buildingArea' => 36, 'landArea' => 72, 'bedrooms' => 2, 'bathrooms' => 1, 'notes' => 'Tipe favorit keluarga muda, cicilan KPR sangat terjangkau.']
            ],
            [
                'cluster_name' => 'Jasmine Spring', 'block' => 'Blok D-09', 'name' => 'Tipe 36/72',
                'selling_price' => 600000000, 'status' => 'sold',
                'details' => ['buildingArea' => 36, 'landArea' => 72, 'bedrooms' => 2, 'bathrooms' => 1, 'notes' => 'Dekat minimarket dan gerbang cluster.']
            ],
            [
                'cluster_name' => 'Jasmine Spring', 'block' => 'Blok D-12', 'name' => 'Tipe 36/72',
                'selling_price' => 600000000, 'status' => 'available',
                'details' => ['buildingArea' => 36, 'landArea' => 72, 'bedrooms' => 2, 'bathrooms' => 1, 'notes' => 'Rumah sudut strategis.']
            ],
        ];

        foreach ($unitsData as $data) {
            Unit::updateOrCreate(
                [
                    'cluster_id' => $clusters[$data['cluster_name']]->id,
                    'block' => $data['block'],
                ],
                [
                    'name' => $data['name'],
                    'selling_price' => $data['selling_price'],
                    'status' => $data['status'],
                    'unit_details' => $data['details']
                ]
            );
        }
    }
}

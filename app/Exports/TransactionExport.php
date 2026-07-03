<?php

namespace App\Exports;

use App\Models\Transaction;
use Maatwebsite\Excel\Concerns\FromCollection;
use Maatwebsite\Excel\Concerns\WithHeadings;
use Maatwebsite\Excel\Concerns\WithMapping;

class TransactionExport implements FromCollection, WithHeadings, WithMapping
{
    protected $ids;

    public function __construct($ids)
    {
        $this->ids = $ids;
    }

    public function collection()
    {
        return Transaction::with(['unit.cluster', 'customer'])->whereIn('id', $this->ids)->get();
    }

    public function headings(): array
    {
        return [
            'Nama Customer',
            'No. KTP',
            'Alamat KTP',
            'No. NPWP',
            'Alamat Surat',
            'No. KK',
            'No. Telp Rumah',
            'No. Telp Seluler',
            'Email',
            'Cluster',
            'Blok / Type',
            'Status Transaksi',
            'Tujuan Pembelian',
            'Sumber Dana',
            'Sistem Pembayaran',
            'Harga Jual Inc. PPN',
            'Rencana Plafond KPR',
            'Tanda Jadi',
            'U.Muka (Transaksi KPR)',
            'U.Muka Pertama',
            'Angsuran Pertama (Cash)',
            'Catatan',
            'Tanggal Transaksi'
        ];
    }

    public function map($transaction): array
    {
        return [
            $transaction->customer->name ?? '-',
            $transaction->customer->nik ?? '-',
            $transaction->customer->address ?? '-',
            $transaction->customer->npwp ?? '-',
            $transaction->customer->alamat_surat ?? '-',
            $transaction->customer->no_kk ?? '-',
            $transaction->customer->no_telepon_rumah ?? '-',
            $transaction->customer->phone ?? '-',
            $transaction->customer->email ?? '-',
            $transaction->unit && $transaction->unit->cluster ? $transaction->unit->cluster->name : '-',
            $transaction->unit ? $transaction->unit->block : '-',
            ucfirst($transaction->status),
            $transaction->details['tujuanPembelian'] ?? '-',
            $transaction->details['sumberDana'] ?? '-',
            $transaction->details['sistemPembayaran'] ?? '-',
            $transaction->details['hargaJualIncPpn'] ?? '-',
            $transaction->details['rencanaPlafondKpr'] ?? '-',
            $transaction->details['tandaJadi'] ?? '-',
            $transaction->details['uMukaTransaksiKpr'] ?? '-',
            $transaction->details['uMukaPertamaTransaksiKpr'] ?? '-',
            $transaction->details['angsuranPertamaTransaksiCash'] ?? '-',
            $transaction->notes ?? '-',
            $transaction->created_at ? $transaction->created_at->format('Y-m-d H:i:s') : '-'
        ];
    }
}

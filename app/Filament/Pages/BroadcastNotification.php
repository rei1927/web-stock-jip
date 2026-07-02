<?php

namespace App\Filament\Pages;

use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Concerns\InteractsWithForms;
use Filament\Forms\Contracts\HasForms;
use Filament\Forms\Form;
use Filament\Pages\Page;
use Filament\Actions\Action;
use App\Services\FCMService;
use Filament\Notifications\Notification;

class BroadcastNotification extends Page implements HasForms
{
    use InteractsWithForms;

    protected static ?string $navigationIcon = 'heroicon-o-speaker-wave';
    protected static ?string $navigationGroup = 'Settings';
    protected static ?string $title = 'Kirim Notifikasi Broadcast';

    protected static string $view = 'filament.pages.broadcast-notification';

    public ?array $data = [];

    public function mount(): void
    {
        $this->form->fill();
    }

    public function form(Form $form): Form
    {
        return $form
            ->schema([
                TextInput::make('title')
                    ->label('Judul Notifikasi')
                    ->required()
                    ->maxLength(255),
                Textarea::make('body')
                    ->label('Isi Pesan')
                    ->required()
                    ->rows(4),
            ])
            ->statePath('data');
    }

    protected function getFormActions(): array
    {
        return [
            Action::make('send')
                ->label('Kirim Sekarang')
                ->submit('send')
                ->color('primary'),
        ];
    }

    public function send(): void
    {
        $data = $this->form->getState();

        $result = FCMService::sendToAll($data['title'], $data['body']);

        if (isset($result['error'])) {
            Notification::make()
                ->title('Gagal mengirim broadcast')
                ->body($result['error'])
                ->danger()
                ->send();
        } else {
            Notification::make()
                ->title('Broadcast Terkirim')
                ->body("Berhasil mengirim ke {$result['success']} perangkat. Gagal: {$result['failure']}.")
                ->success()
                ->send();
                
            $this->form->fill();
        }
    }
}

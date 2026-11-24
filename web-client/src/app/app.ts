import {Component, computed, inject, OnInit, signal} from '@angular/core';
import Keycloak, {KeycloakProfile} from 'keycloak-js';
import {RouterModule, RouterOutlet} from '@angular/router';
import {WalletService} from './service/wallet-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  imports: [RouterOutlet, RouterModule]
})
export class App implements OnInit {
  readonly profile = signal<KeycloakProfile | undefined>(undefined);
  readonly fullName = computed(() => [this.profile()?.firstName, this.profile()?.lastName].filter(Boolean).join(' '));

  private readonly keycloak = inject(Keycloak);
  private readonly walletService = inject(WalletService);

  readonly wallets = this.walletService.wallets;

  ngOnInit(): void {
    this.keycloak.loadUserProfile().then((profile) => this.profile.set(profile));
    this.walletService.loadWallets();
  }

  logout(): void {
    this.keycloak.logout({redirectUri: document.baseURI});
  }
}

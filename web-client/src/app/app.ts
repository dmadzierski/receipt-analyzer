import {Component, computed, inject, OnInit, signal} from '@angular/core';
import Keycloak, {KeycloakProfile} from 'keycloak-js';
import {NavigationEnd, Router, RouterModule, RouterOutlet} from '@angular/router';
import {filter} from 'rxjs';
import {WalletService} from './service/wallet-service';
import {GetWalletListResponseItem} from './model/wallet.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  imports: [RouterOutlet, RouterModule]
})
export class App implements OnInit {
  readonly profile = signal<KeycloakProfile | undefined>(undefined);
  readonly fullName = computed(() => [this.profile()?.firstName, this.profile()?.lastName].filter(Boolean).join(' '));
  readonly wallets = signal<GetWalletListResponseItem[]>([]);

  private readonly keycloak = inject(Keycloak);
  private readonly walletService = inject(WalletService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.keycloak.loadUserProfile().then((profile) => this.profile.set(profile));
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.loadWallets());
  }

  logout(): void {
    this.keycloak.logout({redirectUri: document.baseURI});
  }

  private loadWallets(): void {
    this.walletService.getWalletList().subscribe((result) => this.wallets.set(result.items ?? []));
  }
}

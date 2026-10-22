import {Component, inject, OnInit, signal} from '@angular/core';
import Keycloak, {KeycloakProfile} from 'keycloak-js';
import {NavigationEnd, Router, RouterModule, RouterOutlet} from '@angular/router';
import {filter} from 'rxjs';
import {UserSyncService} from './service/user-sync.service';
import {WalletService} from './service/wallet-service';
import {GetWalletListResponseItem} from './model/wallet.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  imports: [RouterOutlet, RouterModule]
})
export class App implements OnInit {
  profile: KeycloakProfile | undefined;
  wallets = signal<GetWalletListResponseItem[]>([]);

  keycloak = inject(Keycloak);
  private readonly userSync = inject(UserSyncService);
  private readonly walletService = inject(WalletService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.keycloak.loadUserProfile().then((profile) => {
      this.profile = profile;
      this.userSync.syncMeOnce();
    });
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.loadWallets());
  }

  private loadWallets(): void {
    this.walletService.getWalletList().subscribe((result) => this.wallets.set(result.items ?? []));
  }
}

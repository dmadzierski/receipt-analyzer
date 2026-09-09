import {Component, inject, OnInit} from '@angular/core';
import Keycloak, {KeycloakProfile} from 'keycloak-js';
import {RouterModule, RouterOutlet} from '@angular/router';
import {UserSyncService} from './service/user-sync.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  imports: [RouterOutlet, RouterModule]
})
export class App implements OnInit {
  profile: KeycloakProfile | undefined;

  keycloak = inject(Keycloak);
  private readonly userSync = inject(UserSyncService);

  ngOnInit(): void {
    this.keycloak.loadUserProfile().then((profile) => {
      this.profile = profile;
      this.userSync.syncMeOnce();
    });
  }
}

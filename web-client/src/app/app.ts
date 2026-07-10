import {Component, inject, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import Keycloak, {KeycloakProfile} from 'keycloak-js';
import {RouterOutlet, RouterModule} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
  imports: [RouterOutlet, RouterModule]
})
export class App implements OnInit {
  profile: KeycloakProfile | undefined;

  keycloak = inject(Keycloak);

  ngOnInit(): void {
    this.keycloak.loadUserProfile().then((profile) => (this.profile = profile));
  }
}

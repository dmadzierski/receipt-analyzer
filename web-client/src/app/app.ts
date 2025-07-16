import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  userInfo: UserInfo | undefined;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.getMe().subscribe((res) => {
      this.userInfo = res;
    });
  }

  getMe() {
    return this.http.get<UserInfo>('/api/users/me');
  }
}

export interface UserInfo {
  username: string;
  email: string;
  claims: any;
}

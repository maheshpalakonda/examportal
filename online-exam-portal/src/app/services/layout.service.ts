import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LayoutService {
  private _hideNavbar$ = new BehaviorSubject<boolean>(false);
  hideNavbar$ = this._hideNavbar$.asObservable();

  setHideNavbar(v: boolean) {
    this._hideNavbar$.next(v);
  }
}

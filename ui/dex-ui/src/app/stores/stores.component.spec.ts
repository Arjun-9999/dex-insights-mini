import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { StoresComponent } from './stores.component';

describe('StoresComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoresComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads first page on init', () => {
    const fixture = TestBed.createComponent(StoresComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne('/v1/stores?page=0&size=10&sortByOfflinePumps=false');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0 });
  });

  it('applies filters and sort to query params', () => {
    const fixture = TestBed.createComponent(StoresComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    httpMock.expectOne('/v1/stores?page=0&size=10&sortByOfflinePumps=false').flush({ content: [] });

    component.brandFilter = 'Shell';
    component.statusFilter = 'ONLINE';
    component.sortByOfflinePumps = true;
    component.applyFilters();

    const req = httpMock.expectOne('/v1/stores?page=0&size=10&brand=Shell&status=ONLINE&sortByOfflinePumps=true');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [{ STOREID: '001' }], totalElements: 1 });

    expect(component.stores.length).toBe(1);
  });
});


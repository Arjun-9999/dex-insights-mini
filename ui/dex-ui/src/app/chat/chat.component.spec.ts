import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ChatComponent } from './chat.component';

describe('ChatComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads stores on init and auto-selects first store', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    const req = httpMock.expectOne('/v1/stores?page=0&size=100');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [{ STOREID: 'S001', BRAND: 'Shell' }] });

    expect(component.stores.length).toBe(1);
    expect(component.selectedStoreId).toBe('S001');
  });

  it('posts question with storeId when asking', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    httpMock.expectOne('/v1/stores?page=0&size=100').flush({ content: [{ STOREID: 'S002', BRAND: 'BP' }] });

    component.question = 'How is this store performing?';
    component.selectedStoreId = 'S002';
    component.ask();

    const req = httpMock.expectOne('/v1/chat');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      question: 'How is this store performing?',
      storeId: 'S002'
    });

    req.flush({ answer: 'ok', citations: [] });
    expect(component.response.answer).toBe('ok');
  });
});


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

  it('posts question without storeId when no store context is selected', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    httpMock.expectOne('/v1/stores?page=0&size=100').flush({ content: [{ STOREID: 'S002', BRAND: 'BP' }] });

    component.selectedStoreId = '';
    component.question = 'Give me overall platform summary';
    component.ask();

    const req = httpMock.expectOne('/v1/chat');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      question: 'Give me overall platform summary'
    });

    req.flush({ answer: 'summary', citations: [] });
    expect(component.response.answer).toBe('summary');
  });

  it('clears previous response when store context changes', () => {
    const fixture = TestBed.createComponent(ChatComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    httpMock.expectOne('/v1/stores?page=0&size=100').flush({
      content: [
        { STOREID: '10001', BRAND: '7-Eleven' },
        { STOREID: '10002', BRAND: 'Shell' }
      ]
    });

    component.response = {
      answer: "Sorry, I didn't understand that question for Store 10001 (7-Eleven).",
      citations: [{ type: 'store', storeId: '10001' }]
    };

    component.onStoreContextChange('10002');

    expect(component.selectedStoreId).toBe('10002');
    expect(component.response).toBeNull();
    expect(component.errorMessage).toBe('');
  });
});


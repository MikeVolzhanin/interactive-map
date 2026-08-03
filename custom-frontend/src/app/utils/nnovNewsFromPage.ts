/** Публичный HTML проксируется с бэкенда (см. HseNnovHtmlProxyController). */
const HTML_PROXY = '/api/public/hse-nnov-html';
const NNOV_ORIGIN = 'https://nnov.hse.ru';

export type NnovNewsItem = {
  title: string;
  link: string;
  imageUrl: string | null;
  description: string;
  dateLabel: string | null;
  category: string | null;
};

function absolutizeAsset(url: string | null | undefined): string | null {
  if (!url) {
    return null;
  }
  const u = url.trim();
  if (u.startsWith('//')) {
    return `https:${u}`;
  }
  if (u.startsWith('/')) {
    return `${NNOV_ORIGIN}${u}`;
  }
  return u;
}

function isNnovHostname(hostname: string): boolean {
  return hostname.toLowerCase() === 'nnov.hse.ru';
}

export type ParsedNewsPage = {
  items: NnovNewsItem[];
  /** Сколько блоков .post было в разметке (до фильтра по домену). */
  rawPostCount: number;
};

/**
 * Разбор разметки списка новостей как на nnov.hse.ru/news/ (блоки .posts_general .post).
 * Оставляем только материалы со ссылкой на хост nnov.hse.ru.
 */
export function parseNnovNewsListingHtml(html: string): ParsedNewsPage {
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const posts = doc.querySelectorAll('.posts_general .post');
  const rawPostCount = posts.length;
  const out: NnovNewsItem[] = [];

  for (const post of posts) {
    const anchor = post.querySelector('.post__content h2 a');
    if (!anchor) {
      continue;
    }
    const hrefRaw = anchor.getAttribute('href');
    if (!hrefRaw) {
      continue;
    }
    let url: URL;
    try {
      url = new URL(hrefRaw, NNOV_ORIGIN);
    } catch {
      continue;
    }
    if (!isNnovHostname(url.hostname)) {
      continue;
    }
    const link = url.href;

    const title = anchor.textContent?.replace(/\s+/g, ' ').trim() ?? '';
    if (!title) {
      continue;
    }

    const imgSrc = post.querySelector<HTMLImageElement>('.post__content .picture img')?.getAttribute('src');
    const imageUrl = absolutizeAsset(imgSrc);

    const textEl = post.querySelector('.post__content .post__text');
    const description = textEl?.textContent?.replace(/\s+/g, ' ').trim() ?? '';

    const day = post.querySelector('.post-meta__day')?.textContent?.trim();
    const month = post.querySelector('.post-meta__month')?.textContent?.trim();
    const year = post.querySelector('.post-meta__year')?.textContent?.trim();
    const fromMeta = [day, month, year].filter(Boolean).join('\u00a0');
    const dateLabel =
      fromMeta ||
      post.querySelector('.post__content .post__date')?.textContent?.replace(/\s+/g, ' ').trim() ||
      null;

    const category = post.querySelector('.tag-set .rubric span')?.textContent?.replace(/\s+/g, ' ').trim() ?? null;

    out.push({ title, link, imageUrl, description, dateLabel, category });
  }

  return { items: out, rawPostCount };
}

/** Путь для прокси: первая страница — {@code news/}, далее {@code news/pageN.html}. */
export function nnovNewsListingProxyPath(page: number): string {
  if (page < 1) {
    return 'news/';
  }
  if (page === 1) {
    return 'news/';
  }
  return `news/page${page}.html`;
}

export async function fetchNnovNewsHtmlPage(page: number): Promise<string> {
  const path = nnovNewsListingProxyPath(page);
  const url = `${HTML_PROXY}/${path}`;
  const res = await fetch(url, { credentials: 'same-origin' });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}`);
  }
  return res.text();
}

export async function loadNnovNewsPage(page: number): Promise<ParsedNewsPage> {
  if (page === 1) {
    const res = await fetch('/api/news');
    if (res.ok) {
      const backendItems = await res.json();
      if (Array.isArray(backendItems)) {
        const items = backendItems.map((item) => ({
          title: String(item.title ?? ''),
          link: String(item.link ?? ''),
          imageUrl: null,
          description: String(item.text ?? ''),
          dateLabel: String(item.date ?? ''),
          category: null,
        })).filter((item) => item.title && item.link);
        return { items, rawPostCount: items.length };
      }
    }
  }

  if (page > 1) {
    return { items: [], rawPostCount: 0 };
  }

  const html = await fetchNnovNewsHtmlPage(page);
  return parseNnovNewsListingHtml(html);
}

const fs = require('fs');

let content = fs.readFileSync('src/main/resources/templates/index.html', 'utf8');

const replacement = `  <!-- ══════════ COMIC BOTANICAL HERO — 5 SLIDES ══════════ -->
  <section class="comic-hero" id="comicHero" aria-label="Comic Botanical Hero">
    <div class="swiper comic-swiper">
      <div class="swiper-wrapper">
        <!-- SLIDE 1 -->
        <div class="swiper-slide">
          <div class="comic-slide-bg" style="background-image: url('/images/botanical-book-1.png');"></div>
          <div class="comic-slide-content">
            <div class="comic-chapter" th:text="\\$\\{siteContent.home.heroEyebrow}">Botanical Wellness</div>
            <h1 class="comic-heading" th:utext="\\$\\{siteContent.home.heroHeading}">A new theory of<br><em>blooming</em> from within</h1>
            <div class="comic-divider"></div>
            <p class="comic-desc">Crafted by nature, perfected by science — each blend draws from centuries of botanical tradition for serenity, warmth and quiet wisdom.</p>
            <div class="comic-ingredients">
              <span class="comic-tag">Chamomile</span><span class="comic-tag">Lavender</span>
              <span class="comic-tag">Rose Hip</span><span class="comic-tag">Lemon Balm</span>
            </div>
            <a th:href="@{/shop}" class="comic-cta" th:utext="\\$\\{siteContent.home.heroCtaText} + ' &rarr;'">Shop Blends &rarr;</a>
          </div>
        </div>

        <!-- SLIDE 2 -->
        <div class="swiper-slide">
          <div class="comic-slide-bg" style="background-image: url('/images/botanical-book-2.png');"></div>
          <div class="comic-slide-content">
            <div class="comic-chapter">Lunar Calm Blend</div>
            <h2 class="comic-heading">An evening<br><em>ritual for rest</em></h2>
            <div class="comic-divider"></div>
            <p class="comic-desc">Lavender's soft floral note meets the gentle sweetness of rose — a twilight infusion that eases the mind into stillness. Sip slowly, like dusk folding over the day.</p>
            <div class="comic-ingredients">
              <span class="comic-tag">Lavender Buds</span><span class="comic-tag">Rose Petals</span>
              <span class="comic-tag">Valerian Root</span><span class="comic-tag">Passionflower</span>
            </div>
            <a th:href="@{/shop}" class="comic-cta">Explore Lunar Calm &rarr;</a>
          </div>
        </div>

        <!-- SLIDE 3 -->
        <div class="swiper-slide">
          <div class="comic-slide-bg" style="background-image: url('/images/botanical-book-3.png');"></div>
          <div class="comic-slide-content">
            <div class="comic-chapter">Morning Ritual Blend</div>
            <h2 class="comic-heading">Begin each day<br><em>in full bloom</em></h2>
            <div class="comic-divider"></div>
            <p class="comic-desc">Hibiscus — crimson, tart, alive — pairs with antioxidant-rich rose hip to ignite the senses. A botanical sunrise crafted for those who greet the morning with intention.</p>
            <div class="comic-ingredients">
              <span class="comic-tag">Hibiscus Petals</span><span class="comic-tag">Rose Hip</span>
              <span class="comic-tag">Ginger Root</span><span class="comic-tag">Lemongrass</span>
            </div>
            <a th:href="@{/shop}" class="comic-cta">Begin Your Bloom &rarr;</a>
          </div>
        </div>

        <!-- SLIDE 4 -->
        <div class="swiper-slide">
          <div class="comic-slide-bg" style="background-image: url('/images/botanical-book-4.png');"></div>
          <div class="comic-slide-content">
            <div class="comic-chapter">Clarity Bloom Blend</div>
            <h2 class="comic-heading">Still the mind,<br><em>sharpen the focus</em></h2>
            <div class="comic-divider"></div>
            <p class="comic-desc">Ashwagandha — an ancient adaptogen — grounds the nervous system while sharpening mental clarity. Paired with brahmi and tulsi, it supports focus you can feel without the edge of caffeine.</p>
            <div class="comic-ingredients">
              <span class="comic-tag">Ashwagandha</span><span class="comic-tag">Brahmi</span>
              <span class="comic-tag">Tulsi</span><span class="comic-tag">Lion's Mane</span>
            </div>
            <a th:href="@{/shop}" class="comic-cta">Explore Clarity Blend &rarr;</a>
          </div>
        </div>

        <!-- SLIDE 5 -->
        <div class="swiper-slide">
          <div class="comic-slide-bg" style="background-image: url('/images/botanical-book-5.png');"></div>
          <div class="comic-slide-content">
            <div class="comic-chapter">Skin Nectar Blend</div>
            <h2 class="comic-heading">Nourish from<br><em>the inside out</em></h2>
            <div class="comic-divider"></div>
            <p class="comic-desc">Peppermint's cool brightness meets the deep antioxidant power of spearmint and nettle — a ritual for luminous skin. Every sip supports the body's natural glow, quietly and beautifully.</p>
            <div class="comic-ingredients">
              <span class="comic-tag">Peppermint</span><span class="comic-tag">Spearmint</span>
              <span class="comic-tag">Nettle Leaf</span><span class="comic-tag">Rosehip</span>
            </div>
            <a th:href="@{/shop}" class="comic-cta">Discover Skin Nectar &rarr;</a>
          </div>
        </div>
      </div>
      <!-- Controls -->
      <div class="comic-nav">
        <div class="swiper-button-prev comic-prev"></div>
        <div class="swiper-pagination comic-pagination"></div>
        <div class="swiper-button-next comic-next"></div>
      </div>
    </div>
  </section>`;

content = content.replace(/<!-- ══════════ BOTANICAL BOOK HERO — 5 SPREADS ══════════ -->[\s\S]*?<\/section>/, replacement.replace(/\\\\\$\\\{/g, '${'));

const scriptReplacement = `  /* ── Comic Hero Carousel ── */
  const comicSwiper = new Swiper('.comic-swiper', {
    effect: 'fade',
    fadeEffect: { crossFade: true },
    speed: 1200,
    loop: true,
    autoplay: {
      delay: 4500,
      disableOnInteraction: false,
      pauseOnMouseEnter: true
    },
    pagination: {
      el: '.comic-pagination',
      clickable: true,
    },
    navigation: {
      nextEl: '.comic-next',
      prevEl: '.comic-prev',
    },
    keyboard: {
      enabled: true,
    }
  });`;

content = content.replace(/\(function\(\)\{\s*var TOTAL=5,FLIP_MS=900[\s\S]*?\}\)\(\);/, scriptReplacement);

fs.writeFileSync('src/main/resources/templates/index.html', content);
console.log('Successfully updated index.html');

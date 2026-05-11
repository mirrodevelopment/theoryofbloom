const fs = require('fs');
const path = require('path');

const templatesDir = path.join('c:/Users/girit/Downloads/theoryofbloom/theoryofbloom/src/main/resources/templates');
const cssDir = path.join('c:/Users/girit/Downloads/theoryofbloom/theoryofbloom/src/main/resources/static/css');

const newFontUrl = 'https://fonts.googleapis.com/css2?family=Cinzel:wght@400;500;600;700&family=Playfair+Display:ital,wght@0,400;0,500;0,600;0,700;1,400;1,500&family=Inter:wght@300;400;500;600&display=swap';

function processFiles(dir, ext, callback) {
    fs.readdirSync(dir).forEach(file => {
        if (file.endsWith(ext)) {
            const filepath = path.join(dir, file);
            let content = fs.readFileSync(filepath, 'utf8');
            content = callback(content);
            fs.writeFileSync(filepath, content);
        }
    });
}

// 1. Update HTML files to include Cinzel font link
processFiles(templatesDir, '.html', (content) => {
    // Replace old Playfair Display links
    content = content.replace(/<link href="https:\/\/fonts\.googleapis\.com\/css2\?[^>]+>/g, '<link href="' + newFontUrl + '" rel="stylesheet">');
    return content;
});

// 2. Update CSS files to use Cinzel for brand classes
processFiles(cssDir, '.css', (content) => {
    // Update import if it exists
    content = content.replace(/@import url\('https:\/\/fonts.googleapis.com\/css2\?family=Playfair\+Display[^']+'\);/g, '@import url(\'' + newFontUrl + '\');');
    
    // Switch brand texts to Cinzel
    ['\\.shop-nav-brand-text', '\\.auth-brand', '\\.shop-footer-name', '\\.navbar-brand'].forEach(selector => {
        const regex = new RegExp('(' + selector + '[\\s\\S]*?font-family:\\s*)[^;]+;', 'g');
        content = content.replace(regex, '$1\'Cinzel\', serif;');
    });

    // Also inject custom rule for standard elements if missing
    if (!content.includes('font-family: \'Cinzel\', serif')) {
        content = content.replace(/font-family: 'Playfair Display', serif;/g, 'font-family: \'Cinzel\', serif;');
    }
    
    return content;
});

console.log('Fonts updated successfully.');

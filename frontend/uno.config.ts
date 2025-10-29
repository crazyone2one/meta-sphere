import {
    defineConfig,
    presetAttributify,
    presetIcons,
    presetTypography,
    presetWind3,
    transformerDirectives,
    transformerVariantGroup
} from 'unocss'
// loader helpers
import {FileSystemIconLoader} from '@iconify/utils/lib/loader/node-loaders'
import {generateMenuIcons} from "./src/layouts/sidebar/utils";
import {mainRoutes} from "./src/router/routes";

export default defineConfig({
    safelist: [...generateMenuIcons(mainRoutes)],
    shortcuts: {
        'one-line-text': 'overflow-hidden overflow-ellipsis whitespace-nowrap'
    },
    theme: {
        colors: {
            // ...
        }
    },
    presets: [
        presetWind3(),
        presetAttributify(),
        presetIcons({
            // 设置全局图标默认属性
            extraProperties: {
                width: "1em",
                height: "1em",
            },
            collections: {
                'local': FileSystemIconLoader('./src/assets/icons', (svg) => {
                    // 如果 SVG 文件未定义 `fill` 属性，则默认填充 `currentColor`
                    // 这样图标颜色会继承文本颜色，方便在不同场景下适配
                    return svg.includes('fill="')
                        ? svg
                        : svg.replace(/^<svg /, '<svg fill="currentColor" ');
                }),
                antd: () => import('@iconify-json/ant-design/icons.json').then(i => i.default)
            }
        }),
        presetTypography(),
    ],
    transformers: [
        transformerDirectives(),
        transformerVariantGroup(),
    ],
})
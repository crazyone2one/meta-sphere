// 添加全局样式
export const addGlobalStyles = () => {
    const style = document.createElement('style');
    style.textContent = `
    @keyframes float {
      0% {
        transform: translateY(0) translateX(0);
        opacity: 0.5;
      }
      50% {
        opacity: 1;
      }
      100% {
        transform: translateY(-100vh) translateX(${Math.random() * 200 - 100}px);
        opacity: 0;
      }
    }

    .login-card {
      backdrop-filter: blur(10px);
      transform: translateY(0);
    }

    .login-card:hover {
      transform: translateY(-5px);
    }
    .dark {
      color-scheme: dark;
    }
  `;
    document.head.appendChild(style);
};
// 初始化粒子背景
export const initParticles = (isDarkMode: boolean) => {
    // 简单的粒子效果实现
    const particlesContainer = document.getElementById('particles-js');
    if (!particlesContainer) return;

    // 创建粒子
    const createParticle = () => {
        const particle = document.createElement('div');
        const size = Math.random() * 5 + 1;
        const color = isDarkMode
            ? `rgba(100, 149, 237, ${Math.random() * 0.5 + 0.2})`
            : `rgba(75, 0, 130, ${Math.random() * 0.3 + 0.1})`;

        particle.style.cssText = `
      position: absolute;
      width: ${size}px;
      height: ${size}px;
      background: ${color};
      border-radius: 50%;
      top: ${Math.random() * 100}vh;
      left: ${Math.random() * 100}vw;
      box-shadow: 0 0 ${size * 5}px ${color};
      animation: float ${Math.random() * 10 + 10}s linear infinite;
      z-index: 0;
    `;

        particlesContainer.appendChild(particle);

        // 自动移除粒子
        setTimeout(() => {
            particle.remove();
        }, 15000);
    };

    // 初始创建粒子
    for (let i = 0; i < 50; i++) {
        setTimeout(createParticle, i * 100);
    }

    // 持续创建新粒子
    setInterval(createParticle, 1000);
};